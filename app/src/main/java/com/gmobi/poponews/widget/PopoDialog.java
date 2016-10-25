package com.gmobi.poponews.widget;

import java.util.Calendar;

import com.gmobi.poponews.R;
import com.gmobi.poponews.util.SizeHelper;
import com.momock.event.Event;
import com.momock.event.EventArgs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

public class PopoDialog extends Dialog {

    Context context;
    int positiveButtonId;
    int negativeButtonId;
    int layoutId;
    boolean cancelable;
    private DialogInterface.OnClickListener   positiveButtonClickListener,negativeButtonClickListener;
    private Event<EventArgs> event = new Event<EventArgs>();
    


    
    
    public PopoDialog(Context context) {
        super(context);
        this.context = context;
    }
    public PopoDialog(Context context, int theme){
        super(context, theme);
        this.context = context;
    }
    
    public PopoDialog(Context context, int theme, int layoutId, boolean cancelable, 
    		int positiveButtonId,OnClickListener lp, int negativeButtonId,OnClickListener ln){
        super(context, theme);
        this.context = context;
        this.cancelable = cancelable;
        this.layoutId = layoutId;
        this.negativeButtonId = negativeButtonId;
        this.positiveButtonId = positiveButtonId;
        
        positiveButtonClickListener = lp;
        negativeButtonClickListener = ln;
    }
    
    
    public Event<EventArgs> getNotificationEvent() {
		return event;
	}
    
    public void FireEvent() {
    	event.fireEvent(PopoDialog.this, null);
	}
    
    public void setProgress(int per,long contentLength,long downloadLength)
    {
    	 ((ProgressBar) this.findViewById(R.id.progress_update)).setProgress(per);
    	 
    	 ((TextView) this.findViewById(R.id.dialog_message)).setText(SizeHelper.getSize(downloadLength)+"/"+SizeHelper.getSize(contentLength));
    }
    
    
    public void setTimePicker(int curh,int curm, OnTimeChangedListener l)
    {
	    TimePicker tp=(TimePicker)findViewById(R.id.tp);
	    tp.setIs24HourView(true);
	    Calendar c = Calendar.getInstance();
	    tp.setCurrentHour(curh);
	    tp.setCurrentMinute(curm);
	    tp.setOnTimeChangedListener(l);
    }
    
    public int getTimePickerHour()
    {
	    TimePicker tp=(TimePicker)findViewById(R.id.tp);
	    return tp.getCurrentHour(); 
    }
    public int getTimePickerMinute()
    {
	    TimePicker tp=(TimePicker)findViewById(R.id.tp);
	    return tp.getCurrentMinute(); 
    }
    
    

    
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(layoutId);
		this.setCancelable(cancelable);

        View positiveBtn = findViewById(positiveButtonId);
        if(null != positiveBtn) {
            if (positiveButtonClickListener != null) {
                positiveBtn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        positiveButtonClickListener.onClick(
                                PopoDialog.this,
                                DialogInterface.BUTTON_POSITIVE);
                    }
                });
            } else {
                positiveBtn.setVisibility(View.GONE);
            }
        }
        View negativeBtn = findViewById(negativeButtonId);
        if(null != negativeBtn) {
            if (negativeButtonClickListener != null) {
                negativeBtn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        negativeButtonClickListener.onClick(
                                PopoDialog.this,
                                DialogInterface.BUTTON_NEGATIVE);
                    }
                });
            } else {
                negativeBtn.setVisibility(View.GONE);
            }
        }
	}

    
	@Override
	public void show() {
        Window dialogWindow = this.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.BOTTOM);
        dialogWindow.setWindowAnimations(R.style.dialog_animstyle); 
        
		super.show();
	}

//================================================================================================







	/**
     * Helper class for creating a custom dialog
     */
    public static class Builder {
  
        private Context context;
        private String title;
        private String message;
        private String positiveButtonText;
        private String negativeButtonText;
        private View contentView;
        private int layoutId;
  
        private DialogInterface.OnClickListener 
                        positiveButtonClickListener,
                        negativeButtonClickListener;
  
        public Builder(Context context) {
            this.context = context;
        }
  
        /**
         * Set the Dialog message from String
         * @param title
         * @return
         */
        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }
  
        /**
         * Set the Dialog message from resource
         * @param title
         * @return
         */
        public Builder setMessage(int message) {
            this.message = (String) context.getText(message);
            return this;
        }
  
        /**
         * Set the Dialog title from resource
         * @param title
         * @return
         */
        public Builder setTitle(int title) {
            this.title = (String) context.getText(title);
            return this;
        }
  
        /**
         * Set the Dialog title from String
         * @param title
         * @return
         */
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }
  
        /**
         * Set a custom content view for the Dialog.
         * If a message is set, the contentView is not
         * added to the Dialog...
         * @param v
         * @return
         */
        public Builder setContentView(View v) {
            this.contentView = v;
            return this;
        }
  
        /**
         * Set the positive button resource and it's listener
         * @param positiveButtonText
         * @param listener
         * @return
         */
        public Builder setPositiveButton(int positiveButtonText,
                DialogInterface.OnClickListener listener) {
            this.positiveButtonText = (String) context
                    .getText(positiveButtonText);
            this.positiveButtonClickListener = listener;
            return this;
        }
  
        /**
         * Set the positive button text and it's listener
         * @param positiveButtonText
         * @param listener
         * @return
         */
        public Builder setPositiveButton(String positiveButtonText,
                DialogInterface.OnClickListener listener) {
            this.positiveButtonText = positiveButtonText;
            this.positiveButtonClickListener = listener;
            return this;
        }
  
        /**
         * Set the negative button resource and it's listener
         * @param negativeButtonText
         * @param listener
         * @return
         */
        public Builder setNegativeButton(int negativeButtonText,
                DialogInterface.OnClickListener listener) {
            this.negativeButtonText = (String) context
                    .getText(negativeButtonText);
            this.negativeButtonClickListener = listener;
            return this;
        }
  
        /**
         * Set the negative button text and it's listener
         * @param negativeButtonText
         * @param listener
         * @return
         */
        public Builder setNegativeButton(String negativeButtonText,
                DialogInterface.OnClickListener listener) {
            this.negativeButtonText = negativeButtonText;
            this.negativeButtonClickListener = listener;
            return this;
        }
  
        /**
         * Create the custom dialog
         */
        public PopoDialog create() {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // instantiate the dialog with the custom Theme
            final PopoDialog dialog = new PopoDialog(context, 
                    R.style.PopoDialogStyle);
            
            View layout = inflater.inflate(R.layout.dialog_update, null);

            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);

            dialog.addContentView(layout, lp);
            
            /*
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            		600, 600);
            layout.setLayoutParams(lp);
            */
           // ((TextView) layout.findViewById(R.id.dialog_title)).setText(title);
            // set the confirm button
            if (positiveButtonText != null) {
                ((Button) layout.findViewById(R.id.positiveButton))
                        .setText(positiveButtonText);
                if (positiveButtonClickListener != null) {
                    layout.findViewById(R.id.positiveButton)
                            .setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    positiveButtonClickListener.onClick(
                                            dialog,
                                            DialogInterface.BUTTON_POSITIVE);
                                }
                            });
                }
            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.positiveButton).setVisibility(
                        View.GONE);
            }
            // set the cancel button
            if (negativeButtonText != null) {
                ((Button) layout.findViewById(R.id.negativeButton))
                        .setText(negativeButtonText);
                if (negativeButtonClickListener != null) {
                    layout.findViewById(R.id.negativeButton)
                            .setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    positiveButtonClickListener.onClick(
                                            dialog,
                                            DialogInterface.BUTTON_NEGATIVE);
                                }
                            });
                }
            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.negativeButton).setVisibility(
                        View.GONE);
            }

            
            /*
            if (message != null) {
                ((TextView) layout.findViewById(
                        R.id.dialog_message)).setText(message);
            } else if (contentView != null) {
                // if no message set
                // add the contentView to the dialog body
                ((LinearLayout) layout.findViewById(R.id.dialog_content))
                        .removeAllViews();
                ((LinearLayout) layout.findViewById(R.id.dialog_content))
                        .addView(contentView, 
                                new LayoutParams(
                                        LayoutParams.WRAP_CONTENT, 
                                        LayoutParams.WRAP_CONTENT));
            }*/
            dialog.setContentView(layout);
            return dialog;
        }
  
    }

    
	

}
