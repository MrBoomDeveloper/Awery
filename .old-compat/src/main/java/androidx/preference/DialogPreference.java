package androidx.preference;

public abstract class DialogPreference extends Preference {
	public CharSequence title, message;
	
	public CharSequence getDialogTitle() {
		return title;
	}
	
	public void setDialogTitle(CharSequence dialogTitle) {
		this.title = dialogTitle;
	}
	
	public CharSequence getDialogMessage() {
		return message;
	}
	
	public void setDialogMessage(CharSequence dialogMessage) {
		this.message = dialogMessage;
	}
}
