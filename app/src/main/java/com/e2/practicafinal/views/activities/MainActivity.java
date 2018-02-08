package com.e2.practicafinal.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.e2.practicafinal.R;
import com.e2.practicafinal.models.Person;
import com.e2.practicafinal.views.ViewHolders.PersonViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getName();

    @BindView(R.id.recyclerView) RecyclerView mRecycler;

    private DatabaseReference mDatabase;
    private FirebaseRecyclerAdapter<Person, PersonViewHolder> mAdapter;

    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerView.ItemDecoration mItemDecoration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mRecycler.setHasFixedSize(true);

        mItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        mRecycler.addItemDecoration(mItemDecoration);

        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecycler.setLayoutManager(mLinearLayoutManager);

        // Set up FirebaseRecyclerAdapter with the Query
        Query personQuery = mDatabase.child("people").orderByChild("name");

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Person>()
                .setQuery(personQuery, Person.class)
                .build();

        mAdapter = new FirebaseRecyclerAdapter<Person, PersonViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PersonViewHolder holder, int position, @NonNull Person model) {
                final DatabaseReference personRef = getRef(position);
                final String postKey = personRef.getKey();

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MainActivity.this, AddActivity.class);
                        intent.putExtra(AddActivity.EXTRA_PERSON_KEY, postKey);
                        startActivity(intent);
                    }
                });

                holder.bindToPost(model);
            }

            @Override
            public PersonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                return new PersonViewHolder(inflater.inflate(R.layout.row_person, parent, false));
            }
        };
        mRecycler.setAdapter(mAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    @OnClick(R.id.fabAddPerson)
    public void OnFabAddPersonClicked() {
        startActivity(new Intent(MainActivity.this, AddActivity.class));
    }
}
