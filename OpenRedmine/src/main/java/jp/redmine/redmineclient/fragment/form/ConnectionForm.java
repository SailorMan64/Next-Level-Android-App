package jp.redmine.redmineclient.fragment.form;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import com.andreabaccega.widget.FormEditText;

import jp.redmine.redmineclient.R;
import jp.redmine.redmineclient.entity.RedmineConnection;
import jp.redmine.redmineclient.form.helper.FormHelper;

public class ConnectionForm extends FormHelper {
	public FormEditText editName;
	public FormEditText editUrl;
	public FormEditText editToken;
	public EditText editAuthID;
	public EditText editAuthPasswd;
	public Button buttonSave;
	public CheckBox checkHttpAuth;
	public LinearLayout formHttpAuth;
	public CheckBox checkUnsafeConnection;
	public EditText editCertKey;
	public RadioGroup radioTextType;

	public Button buttonAccess;
	// public Button buttonUrl1;
	public Button buttonUrl2;
	public Button buttonUrl3;
	public Button buttonUrl4;

	// ---- Auto-bind state (activated only by buttonUrl4) ----
	private boolean isUpdatingUrl = false;    // guard against recursive changes
	private boolean slugBindingActive = false;
	private String lastAutoSlug = "";         // last slug we appended/updated

	public ConnectionForm(View activity){
		this.setup(activity);
		this.setupDefaults();
	}

	private static Integer[] radioTextTypeIds = {R.id.radioButtonTextile, R.id.radioButtonMarkdown, R.id.radioButtonNone};

	public void setup(View activity){
		editName = (FormEditText)activity.findViewById(R.id.editName);
		editUrl = (FormEditText)activity.findViewById(R.id.editURL);
		editToken = (FormEditText)activity.findViewById(R.id.editToken);
		editAuthID = (EditText)activity.findViewById(R.id.editAuthID);
		editAuthPasswd = (EditText)activity.findViewById(R.id.editAuthPasswd);
		buttonSave = (Button)activity.findViewById(R.id.buttonSave);
		buttonAccess = (Button)activity.findViewById(R.id.buttonAccess);
		formHttpAuth = (LinearLayout)activity.findViewById(R.id.formHttpAuth);
		checkHttpAuth = (CheckBox)activity.findViewById(R.id.checkHttpAuth);
		checkUnsafeConnection = (CheckBox)activity.findViewById(R.id.checkPermitUnsafe);
		editCertKey = (EditText)activity.findViewById(R.id.editCertKey);
		radioTextType = (RadioGroup) activity.findViewById(R.id.radioGroupTextType);

		// buttonUrl1 = (Button)activity.findViewById(R.id.buttonUrl1);
		buttonUrl2 = (Button)activity.findViewById(R.id.buttonUrl2);
		buttonUrl3 = (Button)activity.findViewById(R.id.buttonUrl3);
		buttonUrl4 = (Button)activity.findViewById(R.id.buttonUrl4);

		setupEvents();
	}

	public void setupEvents(){
		checkHttpAuth.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton compoundbutton, boolean flag) {
				performSetEnabled(formHttpAuth,flag);
			}
		});

		// Auto-update URL's trailing slug when Name changes, but ONLY after buttonUrl4 was clicked
		editName.addTextChangedListener(new TextWatcher() {
			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
			@Override public void afterTextChanged(Editable s) {
				if (!slugBindingActive) return;
				String name = s == null ? "" : s.toString();
				applyAutoSlugToUrl(name);
			}
		});

		// Shared logic for buttons 2 & 3 (does NOT activate binding)
		OnClickListener onClickGeneral = new OnClickListener(){
			@Override
			public void onClick(View v) {
				Button btn = (Button)v;
				String regex = (String)btn.getTag();
				String text = editUrl.getText().toString();
				text = TextUtils.isEmpty(text) ? "" : text;
				text = text.replaceAll(regex, "");
				if(regex.startsWith("^")){
					text = btn.getText().toString().concat(text);
				} else if(regex.endsWith("$") && !TextUtils.isEmpty(text)) {
					text = text.concat(btn.getText().toString());
				}
				if(!TextUtils.isEmpty(text)){
					setUrlSafely(text);
				}
				editUrl.requestFocusFromTouch();
				// DO NOT activate binding here
			}
		};
		// buttonUrl1.setOnClickListener(onClickGeneral);
		buttonUrl2.setOnClickListener(onClickGeneral);
		buttonUrl3.setOnClickListener(onClickGeneral);

		// Special handler for buttonUrl4: performs its normal URL change AND activates Name→URL binding
		buttonUrl4.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Button btn = (Button)v;
				String regex = (String)btn.getTag();
				String text = editUrl.getText().toString();
				text = TextUtils.isEmpty(text) ? "" : text;
				text = text.replaceAll(regex, "");
				if(regex.startsWith("^")){
					text = btn.getText().toString().concat(text);
				} else if(regex.endsWith("$") && !TextUtils.isEmpty(text)) {
					text = text.concat(btn.getText().toString());
				}
				if(!TextUtils.isEmpty(text)){
					setUrlSafely(text);
				}
				// Activate binding from now on
				slugBindingActive = true;

				// Immediately apply the current Name as trailing slug
				String currentName = editName.getText() == null ? "" : editName.getText().toString();
				applyAutoSlugToUrl(currentName);

				editUrl.requestFocusFromTouch();
			}
		});
	}

	public void setupDefaults(){
		performSetEnabled(formHttpAuth,checkHttpAuth.isChecked());
	}

	@Override
	public boolean Validate(){
		boolean isValidForm =  ValidateForms(
				editName
				,editUrl
				,editToken
		);
		boolean isValidUrl = validateUrl();
		return isValidForm && isValidUrl && radioTextType.getCheckedRadioButtonId() != -1;
	}

	protected boolean validateUrl(){
		if(TextUtils.isEmpty(editUrl.getText()))
			return true;
		String url = editUrl.getText().toString();
		if(url.matches("^https?://.+")){
			return true;
		} else {
			editUrl.setError(editUrl.getContext().getString(R.string.menu_setting_accessurl_set_schema), null);
			return false;
		}
	}

	public String getUrl(){
		if(ValidateForm(editUrl)){
			return editUrl.getText().toString();
		} else {
			return "";
		}
	}
	public String getAuthID(){
		return checkHttpAuth.isChecked() ? editAuthID.getText().toString() : "";
	}
	public String getAuthPassword(){
		return checkHttpAuth.isChecked() ? editAuthPasswd.getText().toString() : "";
	}
	public void setUnsafeConnection(boolean flag){
		checkUnsafeConnection.setChecked(flag);
	}
	public boolean isUnsafeConnection(){
		return checkUnsafeConnection.isChecked();
	}
	public void setAuthentication(boolean flag){
		checkHttpAuth.setChecked(flag);
		performSetEnabled(formHttpAuth,flag);
	}
	public void setAuthentication(String id, String passwd){
		boolean flag = !("".equals(id) && "".equals(passwd));
		setAuthentication(flag);
		editAuthID.setText(id);
		editAuthPasswd.setText(passwd);
	}
	public void setToken(String token){
		editToken.setText(token);
	}
	public String getToken(){
		return editToken.getText().toString();
	}

	public void setValue(RedmineConnection rd){
		// Populate fields
		editName.setText(rd.getName());
		editUrl.setText(rd.getUrl());
		editToken.setText(rd.getToken());
		checkHttpAuth.setChecked(rd.isAuth());
		editAuthID.setText(rd.getAuthId());
		editAuthPasswd.setText(rd.getAuthPasswd());
		checkUnsafeConnection.setChecked(rd.isPermitUnsafe());
		editCertKey.setText(rd.getCertKey());
		if (radioTextTypeIds.length > rd.getTextType() && rd.getTextType() > 0)
			radioTextType.check(radioTextTypeIds[rd.getTextType()]);
		setupDefaults();

		// Reset auto-binding (binding will be enabled only after buttonUrl4 is clicked)
		slugBindingActive = false;
		lastAutoSlug = "";
	}

	public void getValue(RedmineConnection rd){
		rd.setName(editName.getText().toString());
		rd.setUrl(editUrl.getText().toString());
		rd.setToken(editToken.getText().toString());
		rd.setAuth(checkHttpAuth.isChecked());
		rd.setAuthId(editAuthID.getText().toString());
		rd.setAuthPasswd(editAuthPasswd.getText().toString());
		rd.setPermitUnsafe(checkUnsafeConnection.isChecked());
		rd.setCertKey(editCertKey.getText().toString());
		rd.setTextType(java.util.Arrays.asList(radioTextTypeIds).indexOf(radioTextType.getCheckedRadioButtonId()));
	}

	/* ================= Auto-append helpers ================= */

	private void setUrlSafely(String newUrl){
		isUpdatingUrl = true;
		try {
			editUrl.setText(newUrl);
			editUrl.setSelection(newUrl.length());
		} finally {
			isUpdatingUrl = false;
		}
	}

	/**
	 * After binding is active, append/replace the trailing slug of editUrl with slug(name).
	 * If Name is empty, it removes the previously auto-added slug.
	 */
	private void applyAutoSlugToUrl(String name){
		if (!slugBindingActive) return;

		String baseUrl = editUrl.getText() == null ? "" : editUrl.getText().toString();
		if (TextUtils.isEmpty(baseUrl)) return;

		// Remove previous auto-slug if it's still at the end
		String updated = removeTrailingSlug(baseUrl, lastAutoSlug);

		// Compute new slug from current name
		String slug = slugify(name);

		// Append new slug (if present)
		if (!TextUtils.isEmpty(slug)) {
			updated = stripTrailingSlash(updated) + "/" + slug;
		}

		lastAutoSlug = slug;

		if (!isUpdatingUrl) {
			setUrlSafely(updated);
		}
	}

	private String stripTrailingSlash(String s){
		if (TextUtils.isEmpty(s)) return s;
		while (s.endsWith("/")) s = s.substring(0, s.length()-1);
		return s;
	}

	private String removeTrailingSlug(String url, String slug){
		if (TextUtils.isEmpty(url) || TextUtils.isEmpty(slug)) return url;
		String needle = "/" + slug;
		if (url.endsWith(needle)) {
			return url.substring(0, url.length() - needle.length());
		}
		return url;
	}

	private String slugify(String name){
		if (TextUtils.isEmpty(name)) return "";
		String s = name.trim().toLowerCase();
		// spaces/underscores -> hyphens
		s = s.replaceAll("[\\s_]+", "-");
		// keep only url-safe chars (a-z, 0-9, '-', '.')
		s = s.replaceAll("[^a-z0-9\\-\\.]+", "");
		// collapse multiple hyphens
		s = s.replaceAll("-{2,}", "-");
		// trim leading/trailing hyphens/dots
		s = s.replaceAll("^[-\\.]+|[-\\.]+$", "");
		return s;
	}
}
