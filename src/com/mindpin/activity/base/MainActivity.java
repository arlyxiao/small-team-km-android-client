package com.mindpin.activity.base;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.mindpin.R;
import com.mindpin.Logic.AccountManager;
import com.mindpin.base.activity.MindpinBaseActivity;
import com.mindpin.base.utils.BaseUtils;
import com.mindpin.cache.image.ImageCache;
import com.mindpin.model.Note;
import com.mindpin.model.database.NoteDBHelper;
import com.mindpin.receiver.BroadcastReceiverConstants;
import com.mindpin.widget.adapter.NoteListAdapter;

public class MainActivity extends MindpinBaseActivity {
  public final static int REQUEST_CODE_NEW_TEXT = 0;
	private TextView data_syn_textview;
	private ProgressBar data_syn_progress_bar;
	final private SynDataUIBroadcastReceiver syn_data_broadcast_receiver = new SynDataUIBroadcastReceiver();
  private ListView note_list;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// ע��receiver
		registerReceiver(syn_data_broadcast_receiver, new IntentFilter(
				BroadcastReceiverConstants.ACTION_SYN_DATA_UI));
		
		// load view
		setContentView(R.layout.base_main);
		note_list = (ListView) findViewById(R.id.note_list);
    load_list();
		data_syn_textview = (TextView)findViewById(R.id.main_data_syn_text);
		data_syn_progress_bar = (ProgressBar)findViewById(R.id.main_data_syn_progress_bar);
		start_syn_data();

		update_account_info();			
	}
	
	private void load_list() {
	  List<Note> notes = new ArrayList<Note>();
    try {
      notes = NoteDBHelper.all();
    } catch (Exception e) {
      BaseUtils.toast("��ȡ note �б�ʧ��");
      e.printStackTrace();
    }
    NoteListAdapter note_list_adapter = new NoteListAdapter(this);
    note_list_adapter.add_items(notes);
    note_list.setAdapter(note_list_adapter);
  }

  //ͬ������
	private void start_syn_data() {
		sendBroadcast(new Intent("com.mindpin.action.start_syn_data"));
	}
	
	// �ڽ�����ˢ��ͷ����û���
	private void update_account_info(){
		TextView account_name_textview   = (TextView) findViewById(R.id.account_name);
		ImageView account_avatar_imgview = (ImageView)findViewById(R.id.account_avatar);
		
		account_name_textview.setText(current_user().name);
		ImageCache.load_cached_image(current_user().avatar_url, account_avatar_imgview);
	}
	
	
	public void click_new_text(View view){
	  Intent intent = new Intent();
	  intent.setClass(this, NewNoteActivity.class);
	  startActivityForResult(intent,REQUEST_CODE_NEW_TEXT);
	}
	
	public void click_new_photo(View view){
	  BaseUtils.toast("����ʩ��");
	}
	
	@Override
  protected void onDestroy() {
    super.onDestroy();
    // ע�����receiver��һ��Ҫ��onDestroyʱ��ע�ᣬ�������leak�쳣�����³������
    unregisterReceiver(syn_data_broadcast_receiver);
  }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_about:
			open_activity(AboutActivity.class);
			break;
		case R.id.menu_setting:
			open_activity(MindpinSettingActivity.class);
			break;
		case R.id.menu_account_management:
			open_activity(AccountManagerActivity.class);
			break;
		}

		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		 if(keyCode == KeyEvent.KEYCODE_BACK){
			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this); //����ֻ����this��������appliction_context
			
			builder
				.setTitle(R.string.dialog_close_app_title)
				.setMessage(R.string.dialog_close_app_text)
				.setPositiveButton(R.string.dialog_ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int which) {
							MainActivity.this.finish();
						}
					})
				.setNegativeButton(R.string.dialog_cancel, null)
				.show();
			
			return true;
		 }
		 return super.onKeyDown(keyCode, event);
	}
	
	//��������activity����Ļص�����������
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode != Activity.RESULT_OK){
			return;
		}
		switch(requestCode){
		  case REQUEST_CODE_NEW_TEXT:
		    load_list();
		    break;
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	// ͬ������㲥������
	class SynDataUIBroadcastReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			int progress = intent.getExtras().getInt("progress");
			switch(progress){
			case -1:
				//����
				BaseUtils.toast(R.string.app_data_syn_fail);
				data_syn_progress_bar.setProgress(0);
				data_syn_progress_bar.setVisibility(View.GONE);
				break;
			case 0:
				//��ʼͬ��
				data_syn_textview.setText(R.string.now_syning);
				data_syn_progress_bar.setProgress(0);
				data_syn_progress_bar.setVisibility(View.VISIBLE);
			case 1:
				// ͬ��������
				int current_progress = data_syn_progress_bar.getProgress();
				if (current_progress < 90) {
					data_syn_progress_bar.setProgress(current_progress + 1);
				}
				break;
			case 100:
				// ͬ�����
				data_syn_textview.setText("ͬ�����");
				data_syn_progress_bar.setProgress(100);
				
				AccountManager.touch_last_syn_time();
				update_account_info();
				break;
			case 101:
				// ������ʾͬ������
				long time = AccountManager.last_syn_time();
				String str = BaseUtils.date_string(time);
				data_syn_textview.setText("����ͬ���� " + str);
				data_syn_progress_bar.setVisibility(View.GONE);
				break;
			}
		}
	}
	
}
