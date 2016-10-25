package com.gmobi.poponews.cases.setting;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gmobi.poponews.R;
import com.gmobi.poponews.service.IReportService;
import com.gmobi.poponews.util.AnalysisUtil;
import com.momock.app.App;

public class FeedBackFragment extends Fragment {

	TextView btnSend;
	EditText feedbackData;
	IReportService reportSvc;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		reportSvc = App.get().getService(IReportService.class);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		feedbackData.getText().clear();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		AnalysisUtil.recordMeFeedBack();


		View root = inflater.inflate(R.layout.fragment_feedback, container, false);


		btnSend = (TextView) root.findViewById(R.id.feedback_send);
		btnSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (feedbackData.getText().length() == 0) {
					Toast.makeText(getActivity(), getString(R.string.feedback_empty), Toast.LENGTH_SHORT).show();
				} else {
					reportSvc.recordFeedback(feedbackData.getText().toString());
					feedbackData.getText().clear();
					Toast.makeText(App.get(), App.get().getResources().getString(R.string.feedback_sent), Toast.LENGTH_SHORT).show();
					AnalysisUtil.recordMeFeedbackSend();
				}
			}
		});
		feedbackData = (EditText) root.findViewById(R.id.feedback_data);
		feedbackData.getText().clear();
		return root;
	}
}
