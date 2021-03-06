package com.teamkn.model;

import java.io.Serializable;

import com.teamkn.model.base.BaseModel;

public class User extends BaseModel implements Serializable{
  /**
	 * 
	 */
	private static final long serialVersionUID = 3L;
final public static User NIL_USER = new User();
  public int id;
  public int user_id;
  public String user_name;
  public byte[] user_avatar;
  public String avatar_url;
  public long server_created_time;
  public long server_updated_time;
  
  public int count;  // 记录有几条修改建议
  
  private User() {
      set_nil();
  }

//  public User(int id, int user_id, String user_name, byte[] user_avatar,
//      long server_created_time, long server_updated_time) {
//    this.id = id;
//    this.user_id = user_id;
//    this.user_name = user_name;
//    this.user_avatar = user_avatar;
//    this.server_created_time = server_created_time;
//    this.server_updated_time = server_updated_time;
//  }
  
 @Override
	public String toString() {
		return id + " : " + user_id + " : "
				+ server_created_time + " : " + user_name + " : " + user_avatar + " : "+ avatar_url+" : " + server_updated_time;
	}
 

public User(int id, int user_id, String user_name, byte[] user_avatar,
		String avatar_url, long server_created_time, long server_updated_time) {
	super();
	this.id = id;
	this.user_id = user_id;
	this.user_name = user_name;
	this.user_avatar = user_avatar;
	this.avatar_url = avatar_url;
	this.server_created_time = server_created_time;
	this.server_updated_time = server_updated_time;
}

public String getUser_name() {
	return user_name;
}

public void setUser_name(String user_name) {
	this.user_name = user_name;
}

public byte[] getUser_avatar() {
	return user_avatar;
}

public void setUser_avatar(byte[] user_avatar) {
	this.user_avatar = user_avatar;
}

public int getCount() {
	return count;
}

public void setCount(int count) {
	this.count = count;
}

public String getAvatar_url() {
	return avatar_url;
}

public void setAvatar_url(String avatar_url) {
	this.avatar_url = avatar_url;
}

}
