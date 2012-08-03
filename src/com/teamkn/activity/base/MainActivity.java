package com.teamkn.activity.base;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.teamkn.R;
import com.teamkn.Logic.TeamknPreferences;
import com.teamkn.activity.contact.ContactsActivity;
import com.teamkn.activity.note.EditNoteActivity;
import com.teamkn.activity.note.NoteListActivity;
import com.teamkn.activity.usermsg.UserMsgActivity;
import com.teamkn.application.TeamknApplication;
import com.teamkn.base.activity.TeamknBaseActivity;
import com.teamkn.base.task.IndexTimerTask;
import com.teamkn.base.utils.BaseUtils;
import com.teamkn.base.utils.SharedParam;
import com.teamkn.model.AccountUser;
import com.teamkn.model.Note;
import com.teamkn.model.database.NoteDBHelper;
import com.teamkn.service.FaceCommentService;
import com.teamkn.service.IndexService;
import com.teamkn.service.RefreshContactStatusService;
import com.teamkn.service.SynChatService;
import com.teamkn.service.SynNoteService;
import com.teamkn.service.SynNoteService.SynNoteBinder;
import com.teamkn.widget.adapter.NoteListAdapter;

public class MainActivity extends TeamknBaseActivity {
	
	public class RequestCode {
        public final static int EDIT_TEXT = 1;
    }
	//  node_listView_show 数据
	private ListView note_list;
	// 定义每一页显示行数
    private int VIEW_COUNT = 20;  
    // 定义的页数
    private int index = 0;   
    // 当前页
    private int currentPage = 1;     
    // 所以数据的条数
    private int totalCount;     
    // 每次取的数据，只要最后一次可能不一样。
    private int maxResult;	
    
    NoteListAdapter note_list_adapter;
    List<Note> notes;
    // 标记：上次的ID
    private boolean isUpdating = true;
    
    View view;
    
    private LinearLayout mLoadLayout;   

    private final LayoutParams mProgressBarLayoutParams = new LinearLayout.LayoutParams(   

            LinearLayout.LayoutParams.WRAP_CONTENT,   

            LinearLayout.LayoutParams.WRAP_CONTENT); 
	//
	
	private TextView data_syn_textview;         // 同步更新时间
	private ProgressBar data_syn_progress_bar;  // 同步更新进度条
	private ImageView manual_syn_bn;
	private SynNoteBinder syn_note_binder;      // 同步更新binder 
	private SynUIBinder syn_ui_binder = new SynUIBinder();
	
	
	private ServiceConnection conn = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			syn_note_binder = (SynNoteBinder)service;
			syn_note_binder.set_syn_ui_binder(syn_ui_binder);
			syn_note_binder.start();
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
		  // 当 SynNoteService 因异常而断开连接的时候，这个方法才会被调用
		  System.out.println("ServiceConnection  onServiceDisconnected");
		  syn_note_binder = null;
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
//		 LayoutInflater inflater = LayoutInflater.from(this);
//	     view = inflater.inflate(R.layout.progress, null);
		 mLoadLayout = new LinearLayout(this);   

	        mLoadLayout.setMinimumHeight(60);   

	        mLoadLayout.setGravity(Gravity.CENTER);   

	        mLoadLayout.setOrientation(LinearLayout.HORIZONTAL);   

	           

	        ProgressBar mProgressBar = new ProgressBar(this);   

	        mProgressBar.setPadding(0, 0, 15, 0);   

	        mLoadLayout.addView(mProgressBar, mProgressBarLayoutParams);   

	           

	        TextView mTipContent = new TextView(this);   

	        mTipContent.setText("加载中...");   

	        mLoadLayout.addView(mTipContent, mProgressBarLayoutParams);   

	        mLoadLayout.setVisibility(View.GONE);   
		
		// load view
		setContentView(R.layout.base_main);
		data_syn_textview = (TextView)findViewById(R.id.main_data_syn_text);
		data_syn_progress_bar = (ProgressBar)findViewById(R.id.main_data_syn_progress_bar);
		manual_syn_bn = (ImageView)findViewById(R.id.manual_syn_bn);
		
		note_list = (ListView) findViewById(R.id.note_list);
		note_list.addFooterView(mLoadLayout);
		
		// 注册更新服务
		Intent intent = new Intent(MainActivity.this,SynNoteService.class);
		bindService(intent, conn, Context.BIND_AUTO_CREATE);
		   
		// 开始后台索引服务
		IndexService.start(this);
		IndexTimerTask.index_task(IndexTimerTask.SCHEDULE_INTERVAL);
		
		// 注册更新表情反馈服务
		Intent intent1 = new Intent(MainActivity.this,FaceCommentService.class);
		startService(intent1);
		SharedParam.saveParam(this, 0);
		FaceCommentService.context = this;
    
		// 设置用户头像和名字
		AccountUser user = current_user();
		byte[] avatar = user.avatar;
		String name = current_user().name;
		RelativeLayout rl = (RelativeLayout)findViewById(R.id.main_user_avatar);
		if(avatar != null){
			Bitmap bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(avatar));
			Drawable drawable = new BitmapDrawable(bitmap);
			rl.setBackgroundDrawable(drawable);
		}else{
		    rl.setBackgroundResource(R.drawable.user_default_avatar_normal);
		}
	    TextView user_name_tv = (TextView)findViewById(R.id.main_user_name);
	    user_name_tv.setText(name);
    
		// 启动刷新联系人状态服务
		startService(new Intent(MainActivity.this,RefreshContactStatusService.class));
		// 启动更新 对话串的服务
		startService(new Intent(MainActivity.this,SynChatService.class));
		
		//加载node_listview
		load_list();
		
	}
	private int getMaxResult() {
        int totalPage = (totalCount + VIEW_COUNT - 1) / VIEW_COUNT;
        if(currentPage == totalPage-1){
        	 return totalCount - (totalPage - 1) * VIEW_COUNT;
        }
        return VIEW_COUNT;
    }
	//加载node_listview
	private void load_list() {
		try {
			totalCount = NoteDBHelper.getCount();
		} catch (Exception e) {
			e.printStackTrace();
		}
        maxResult = getMaxResult();
		notes  = new ArrayList<Note>();
        try {
        	   notes=NoteDBHelper.getAllItems(index, maxResult);
        	   System.out.println(" -----  " + index + "  :  " + maxResult + "  :  " + notes.size() + " :  " + totalCount);
//            notes =  NoteDBHelper.all(false);
        } catch (Exception e) {
            BaseUtils.toast("读取 note 列表失败");
            e.printStackTrace();
        }
        note_list_adapter = new NoteListAdapter(this);
        note_list_adapter.add_items(notes);
        note_list.setAdapter(note_list_adapter);
        
        note_list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> list_view,View list_item,int item_id,long position) {
            	
                TextView info_tv = (TextView) list_item.findViewById(R.id.note_info_tv);
                String   uuid    = (String)   info_tv.getTag(R.id.tag_note_uuid);
                String   kind    = (String)   info_tv.getTag(R.id.tag_note_kind);
                
                Intent   intent  = new Intent(MainActivity.this, EditNoteActivity.class);
                intent.putExtra(EditNoteActivity.Extra.NOTE_UUID, uuid);
                intent.putExtra(EditNoteActivity.Extra.NOTE_KIND, kind);
                
                if (kind == NoteDBHelper.Kind.IMAGE) {
                    String image_path = Note.note_image_file(uuid).getPath();
                    intent.putExtra(EditNoteActivity.Extra.NOTE_IMAGE_PATH,image_path);
                }
                startActivityForResult(intent,MainActivity.RequestCode.EDIT_TEXT);
            }
        });

        note_list.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {
                menu.add(Menu.NONE, 0, 0, "删除");
            }
        });
        
        
        note_list.setOnScrollListener(new OnScrollListener() {			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
         	   
         	  if (firstVisibleItem + visibleItemCount == totalItemCount && isUpdating) {
                  if (totalItemCount < totalCount) { // 防止最后一次取数据进入死循环。
                	  isUpdating=false;
                	  ++currentPage;
                	  Toast.makeText(MainActivity.this,"正在取第" + (currentPage) + "的数据",Toast.LENGTH_LONG).show();
//                    note_list.addFooterView(view);
                	  
                	  mLoadLayout.setVisibility(View.VISIBLE);
                	  AsyncUpdateDatasTask asyncUpdateWeiBoDatasTask = new AsyncUpdateDatasTask();
                      asyncUpdateWeiBoDatasTask.execute();
                      
                  }
                  System.out.println("begin update-------------");
              }
         	   
         	    
			}
		});
    }
	class AsyncUpdateDatasTask extends AsyncTask<Void, Void, List<Note> > {
		 
        @Override
        protected List<Note> doInBackground(Void... params) {
            index += VIEW_COUNT;
            List<Note> list = new ArrayList<Note>();
            try {
				list = NoteDBHelper.getAllItems(index, maxResult);
			} catch (Exception e) {
				e.printStackTrace();
			}
            System.out.println("doInBackground  : " + list.size());
            return list;
        }
        @Override
        protected void onPostExecute(List<Note> noteOnther) {
            super.onPostExecute(noteOnther);
//            notes.addAll(noteOnther);
            note_list_adapter.add_items(noteOnther);
            note_list_adapter.notifyDataSetChanged();
            isUpdating=true;
            mLoadLayout.setVisibility(View.GONE); 
            System.out.println("end update--------------");
        }
    } 	
	
	//listview的长按事件
	@Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo;
        menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        TextView note_info_tv = (TextView) menuInfo.targetView.findViewById(R.id.note_info_tv);
        String   uuid  = (String) note_info_tv.getTag(R.id.tag_note_uuid);
        destroy_note_confirm(uuid);

        return super.onContextItemSelected(item);
    }
	//listview的长按事件的删除方法
	protected void destroy_note_confirm(final String uuid) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this); //这里只能用this，不能用appliction_context

        builder.setMessage("确认要删除吗？")
               .setPositiveButton(R.string.dialog_ok,
                       new DialogInterface.OnClickListener() {
                           public void onClick(DialogInterface dialog,
                                               int             which) {
                               NoteDBHelper.destroy(uuid);
                               // 有要改进的地方 如 记忆从别的地方回来，还要回到上次加载的地方
                               load_list();
                           }
                       })
               .setNegativeButton(R.string.dialog_cancel, null)
               .show();
    }
	// 处理其他activity界面的回调  有要改进的地方 如 记忆从别的地方回来，还要回到上次加载的地方
    @Override
    protected void onActivityResult(int  requestCode, int resultCode,Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case MainActivity.RequestCode.EDIT_TEXT:
                load_list();
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
	
    
    
	//同步
	public void click_manual_syn(View view){
		if(syn_note_binder != null){
		    syn_note_binder.manual_syn();
		}
	}
	
	public void show_note_list(View view){
	    open_activity(NoteListActivity.class);
	}
	
	public void click_headbar_button_contacts(View view){
	    open_activity(ContactsActivity.class);
	}
	
	public void click_update_user_msg(View view){
		open_activity(UserMsgActivity.class);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 解除 和 更新笔记服务的绑定
		unbindService(conn);
		// 关闭更新联系人状态服务
		stopService(new Intent(MainActivity.this,RefreshContactStatusService.class));
		// 关闭更新对话串的服务
		stopService(new Intent(MainActivity.this,SynChatService.class));
		IndexService.stop();
		stopService(new Intent(MainActivity.this,FaceCommentService.class));
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
			open_activity(TeamknSettingActivity.class);
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
			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this); //这里只能用this，不能用appliction_context
			
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
	
	 public class SynUIBinder{
	    public void set_max_num(int max_num){
	      final int num = max_num;
	      System.out.println("set_max_num   " + max_num);
	      data_syn_progress_bar.post(new Runnable() {
	          @Override
	          public void run() {
	            data_syn_progress_bar.setMax(num);
	          }
          });
	    }
	    
	    public void set_start_syn(){
	      System.out.println("set_start_syn");
	      data_syn_textview.post(new Runnable() {
		      @Override
		      public void run() {
		        data_syn_textview.setText(R.string.now_syning);
		        data_syn_progress_bar.setProgress(0);
		        ///////////////////////////////////////
		        data_syn_progress_bar.setVisibility(View.VISIBLE);
		      }
          });
	    }
	    
	    public void set_progress(int progress){
	      final int num = progress;
	      System.out.println("set_progress  " + progress);
	      data_syn_progress_bar.post(new Runnable() {
		      @Override
		      public void run() {
		        data_syn_progress_bar.setProgress(num);
		        data_syn_progress_bar.setVisibility(View.VISIBLE);
		        manual_syn_bn.setVisibility(View.GONE);
		      }
          });
	    }
	    
	    public void set_syn_success(){
			System.out.println("syn_success");
			data_syn_textview.post(new Runnable() {
				@Override
				public void run() {
					String str = BaseUtils.date_string(TeamknPreferences.last_syn_success_client_time()); 
					data_syn_textview.setText("上次同步成功: " + str);
					data_syn_progress_bar.setVisibility(View.GONE);
					manual_syn_bn.setVisibility(View.VISIBLE);
				}
			});
			  
			if(TeamknApplication.current_show_activity == null 
			    || !TeamknApplication.current_show_activity.equals("com.teamkn.activity.base.MainActivity")){
			    // TODO 增加通知提示
			}
	    }
  
      public void set_syn_fail() {
        System.out.println("syn_fail");
        TeamknPreferences.touch_last_syn_fail_client_time();
        data_syn_textview.post(new Runnable() {
			  @Override
			  public void run() {
			    String str = BaseUtils.date_string(TeamknPreferences.last_syn_fail_client_time()); 
			    data_syn_textview.setText("上次同步失败: " + str);
			    data_syn_progress_bar.setVisibility(View.GONE);
			    manual_syn_bn.setVisibility(View.VISIBLE);
			  }
        });
      }
	  }
}
