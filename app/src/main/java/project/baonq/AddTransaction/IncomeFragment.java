package project.baonq.AddTransaction;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import project.baonq.enumeration.TransactionGroupType;
import project.baonq.menu.R;
import project.baonq.model.DaoSession;
import project.baonq.model.TransactionGroup;
import project.baonq.service.App;
import project.baonq.service.TransactionGroupService;

public class IncomeFragment extends Fragment {
    public IncomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final SelectCategory activity = (SelectCategory) getActivity();
        final List<TransactionGroup> list =
                new TransactionGroupService(activity.getApplication())
                        .findIncomeGroupByLedgerId(activity.getLedgerId());

        View view = inflater.inflate(R.layout.fragment_income, null);

        LinearLayout layout = (LinearLayout) view.findViewById(R.id.linear_layout);
        layout.setBackgroundColor(Color.parseColor("#FFFFFF"));
        for (int i = 0; i < list.size(); i++)
            if (list.get(i).getTransaction_type() == TransactionGroupType.INCOME.getType()) {
                Button txt = new Button(getActivity());
                txt.setText(list.get(i).getName());
                txt.setTextColor(Color.parseColor("#000000"));
                txt.setBackgroundColor(Color.TRANSPARENT);
                final int finalI = i;
                txt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferences pre = getActivity().getSharedPreferences("transaction_data", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = pre.edit();
                        editor.putString("catId", list.get(finalI).getId() + "");
                        editor.commit();
                        getActivity().finish();
                    }
                });
                layout.addView(txt);
            }
        return view;
    }
}
