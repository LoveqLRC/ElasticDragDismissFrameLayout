package com.loveqrc.elasticdragdismissframelayout;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.loveqrc.elasticdragdismissframelayout.widget.ElasticDragDismissFrameLayout;

public class ElasticDragActivity extends Activity {

    private RecyclerView mRvList;
    private ElasticDragDismissFrameLayout mElasticFrameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elastic_drag);
        mElasticFrameLayout = findViewById(R.id.eddfl);
        mRvList = findViewById(R.id.rv_list);
        mRvList.setAdapter(new MyAdapter());
        mRvList.setLayoutManager(new LinearLayoutManager(this));

        mElasticFrameLayout.addListener(new ElasticDragDismissFrameLayout.ElasticDragDismissCallback() {

            @Override
            public void onDragDismissed() {
                ElasticDragActivity.this.finish();
            }
        });
    }
}
