package jp.co.internous.sirius.controller;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import jp.co.internous.sirius.model.domain.MstUser;
import jp.co.internous.sirius.model.form.UserForm;
import jp.co.internous.sirius.model.mapper.MstUserMapper;
import jp.co.internous.sirius.model.mapper.TblCartMapper;
import jp.co.internous.sirius.model.session.LoginSession;


@RestController
@RequestMapping("/sirius/auth")
public class AuthController {
	@Autowired
	private LoginSession loginSession;

	@Autowired
	private MstUserMapper mstUserMapper;

	@Autowired
	private TblCartMapper tblCartMapper;

	private Gson gson = new Gson();
	
	

	//ログイン機能
	@RequestMapping("/login")
	public String login(@RequestBody UserForm f){
		MstUser user = mstUserMapper.findByUserNameAndPassword(f.getUserName(),f.getPassword());
		
		int tmpUserId = loginSession.getTmpUserId();
		
		if (user != null && tmpUserId != 0) {
			int count = tblCartMapper.findCountByUserId(tmpUserId);
			if (count > 0) {
				tblCartMapper.updateUserId(user.getId(), tmpUserId);
			}
		}
		
		if(user != null){
			loginSession.setUserId(user.getId());
			loginSession.setTmpUserId(0);
			loginSession.setUserName(user.getUserName());
			loginSession.setPassword(user.getPassword());
			loginSession.setLoginFlag(true);
		}else{
			loginSession.setUserId(0);
			loginSession.setUserName(null);
			loginSession.setPassword(null);
			loginSession.setLoginFlag(false);
		}

		return gson.toJson(user);
	}

	//ログアウト機能
	@RequestMapping("/logout")
	public String logout(){
		loginSession.setUserId(0);
		loginSession.setTmpUserId(0);
		loginSession.setUserName(null);
		loginSession.setPassword(null);
		loginSession.setLoginFlag(false);
		
		return "";
		
	}
	
	//パスワード再設定
	@RequestMapping("/resetPassword")
	public String resetPassword(@RequestBody UserForm f) {
		String newPassword = f.getNewPassword();
		String newPasswordConfirm = f.getNewPasswordConfirm();
		
		MstUser user = mstUserMapper.findByUserNameAndPassword(loginSession.getUserName(), f.getPassword());
		String message = "";
		
		//認証
		if(user == null) {
			message = "現在のパスワードが正しくありません。";
		} else if(user.getPassword().equals(newPassword)) {
			message = "現在のパスワードと同一文字列が入力されました。";
		} else if(!newPassword.equals(newPasswordConfirm)) {
			message = "新パスワードと確認用パスワードが一致しません。";
		} else {
			// DBの会員情報マスタテーブルのパスワードを更新
			mstUserMapper.updatePassword(user.getUserName(), f.getNewPassword());
			//セッションのパスワードを入力値で更新する。
			loginSession.setPassword(f.getNewPassword());
			message = "パスワードが再設定されました。";
		}
		return message;
	}
}
