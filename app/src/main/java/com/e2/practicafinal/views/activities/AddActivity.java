package com.e2.practicafinal.views.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.e2.practicafinal.R;
import com.e2.practicafinal.models.Person;
import com.e2.practicafinal.models.Pet;
import com.e2.practicafinal.utilities.GlideApp;
import com.e2.practicafinal.utilities.PathUtil;
import com.e2.practicafinal.views.ViewHolders.PetViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getName();
    public static final String EXTRA_PERSON_KEY = "person_key";

    @BindView(R.id.etName) EditText etName;
    @BindView(R.id.etPhone) EditText etPhone;
    @BindView(R.id.tvYourPets) TextView tvYourPets;
    @BindView(R.id.recyclerView) RecyclerView mRecycler;
    @BindView(R.id.fabAddPet) FloatingActionButton mFabAddPet;

    private StorageReference mStorageReference;
    private DatabaseReference mDatabase;
    private DatabaseReference mPersonReference;
    private ValueEventListener mPersonListener;
    private FirebaseRecyclerOptions options;

    private FirebaseRecyclerAdapter<Pet, PetViewHolder> mAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerView.ItemDecoration mItemDecoration;

    MaterialDialog mDialog;
    ImageView ivPet;

    int SELECT_PICTURE = 5001;
    Uri downloadUrl;
    Uri selectedImageUri;
    String selectedImagePath;

    private String mPersonKey;
    private String mPetKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        ButterKnife.bind(this);

        if (getActionBar() != null)
            getActionBar().setDisplayHomeAsUpEnabled(true);

        // Get post key from intent
        mPersonKey = getIntent().getStringExtra(EXTRA_PERSON_KEY);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();

        if (!TextUtils.isEmpty(mPersonKey))
            InitPet();
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
        if (mPersonListener != null) {
            mPersonReference.removeEventListener(mPersonListener);
        }

        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Getting image
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                selectedImageUri = data.getData();

                try {
                    selectedImagePath = PathUtil.getPath(this, selectedImageUri);
                } catch (URISyntaxException e) {
                    Log.e(TAG, e.toString());
                }

                if (ivPet != null && !TextUtils.isEmpty(selectedImagePath))
                    GlideApp.with(this).load(selectedImagePath).circleCrop().into(ivPet);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If mPersonKey has value, edit mode activated
        if (!TextUtils.isEmpty(mPersonKey)) {
            menu.findItem(R.id.action_add_person).setVisible(false);
            menu.findItem(R.id.action_delete_person).setVisible(true);
            menu.findItem(R.id.action_update_person).setVisible(true);
        } else {
            menu.findItem(R.id.action_add_person).setVisible(true);
            menu.findItem(R.id.action_delete_person).setVisible(false);
            menu.findItem(R.id.action_update_person).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;

            case R.id.action_add_person:
                if (IsValidPerson()) {
                    Person person = new Person(etName.getText().toString().trim(),
                            etPhone.getText().toString().trim());

                    String key = mDatabase.child("people").push().getKey();
                    Map<String, Object> personValues = person.toMap();
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("/people/" + key, personValues);
                    mDatabase.updateChildren(childUpdates);

                    Toast.makeText(AddActivity.this, R.string.created, Toast.LENGTH_SHORT).show();
                    mPersonKey = key;
                    InitPet();
                    AddActivity.this.invalidateOptionsMenu();
                    new MaterialDialog.Builder(this)
                            .content(R.string.on_added_person_message)
                            .positiveText(R.string.ok)
                            .show();
                }
                break;

            case R.id.action_delete_person:
                if (TextUtils.isEmpty(mPersonKey))
                    break;

                mPersonReference.removeValue();
                Toast.makeText(AddActivity.this, R.string.deleted, Toast.LENGTH_SHORT).show();
                finish();
                break;

            case R.id.action_update_person:
                if (TextUtils.isEmpty(mPersonKey))
                    break;

                if (IsValidPerson()) {
                    mPersonReference.child("name").setValue(etName.getText().toString().trim());
                    mPersonReference.child("phone").setValue(etPhone.getText().toString().trim());

                    Toast.makeText(AddActivity.this, R.string.updated, Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @SuppressLint("CutPasteId")
    @OnClick(R.id.fabAddPet)
    public void OnFabAddPetClicked(FloatingActionButton fabButton) {

        if (fabButton != null)
            mPetKey = "";

        final View positiveAction;
        final EditText etName;
        final EditText etAge;
        final TextView tvAddPhoto;
        final TextView tvDeletePhoto;

        String acceptText = getString(R.string.create);
        String titleText = getString(R.string.add_pet);
        String neutralText = "";

        if (!TextUtils.isEmpty(mPersonKey) && !TextUtils.isEmpty(mPetKey)) {
            acceptText = getString(R.string.update);
            neutralText = getString(R.string.delete);
            titleText = getString(R.string.edit_pet);
        }

        mDialog = new MaterialDialog.Builder(this)
                .title(titleText)
                .customView(R.layout.dialog_pet, true)
                .positiveText(acceptText)
                .neutralText(neutralText)
                .negativeText(R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull DialogAction which) {
                        if (ivPet.getDrawable() == null)
                            uploadPet();
                        else uploadPetWithPhoto();
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        mPersonReference.child("pets").child(mPetKey).removeValue();
                        mPetKey = "";
                        Toast.makeText(AddActivity.this, R.string.deleted_pet, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        mPetKey = "";
                        dialog.dismiss();
                    }
                })
                .build();

        positiveAction = mDialog.getActionButton(DialogAction.POSITIVE);

        //noinspection ConstantConditions
        etName = mDialog.getCustomView().findViewById(R.id.etName);
        etAge = mDialog.getCustomView().findViewById(R.id.etAge);
        ivPet = mDialog.getCustomView().findViewById(R.id.ivPet);
        tvAddPhoto = mDialog.getCustomView().findViewById(R.id.tvAddPhoto);
        tvDeletePhoto = mDialog.getCustomView().findViewById(R.id.tvDeletePhoto);

        tvAddPhoto.setPaintFlags(tvAddPhoto.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvDeletePhoto.setPaintFlags(tvDeletePhoto.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                positiveAction.setEnabled(s.toString().trim().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        tvAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(intent, getString(R.string.select_photo)), SELECT_PICTURE);
            }
        });

        tvDeletePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ivPet.setImageResource(android.R.color.transparent);
                ivPet.destroyDrawingCache();
            }
        });

        if (!TextUtils.isEmpty(mPersonKey) && !TextUtils.isEmpty(mPetKey)) {
            tvDeletePhoto.setVisibility(View.VISIBLE);
            tvAddPhoto.setText(R.string.update_photo);

            DatabaseReference petReference = mPersonReference.child("pets").child(mPetKey);

            // Add value event listener to the pet
            // [START pet_value_event_listener]
            ValueEventListener petListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Get pet object and use the values to update the UI
                    Pet pet = dataSnapshot.getValue(Pet.class);

                    if (pet == null)
                        return;

                    etName.setText(pet.name);
                    etAge.setText(String.valueOf(pet.age));

                    if (!TextUtils.isEmpty(pet.thumbnail))
                        GlideApp.with(AddActivity.this).load(pet.thumbnail).circleCrop().into(ivPet);
                    else {
                        tvDeletePhoto.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            petReference.addListenerForSingleValueEvent(petListener);
            // [END pet_value_event_listener]
        }

        mDialog.show();
        positiveAction.setEnabled(false); // disabled by default
    }

    private void InitPet() {
        mPersonReference = FirebaseDatabase.getInstance()
                .getReference().child("people").child(mPersonKey);

        // Add value event listener to the person_
        // [START person_value_event_listener]
        ValueEventListener personListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Person object and use the values to update the UI
                Person person = dataSnapshot.getValue(Person.class);

                if (person == null)
                    return;

                etName.setText(person.name);
                etPhone.setText(person.phone);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mPersonReference.addValueEventListener(personListener);
        // [END person_value_event_listener]

        // Keep copy of post listener so we can remove it when app stops
        mPersonListener = personListener;

        mFabAddPet.setVisibility(View.VISIBLE);
        tvYourPets.setVisibility(View.VISIBLE);

        // Set up recycler view
        mRecycler = findViewById(R.id.recyclerView);
        mRecycler.setHasFixedSize(true);

        mItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        mRecycler.addItemDecoration(mItemDecoration);

        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecycler.setLayoutManager(mLinearLayoutManager);

        // Set up FirebaseRecyclerAdapter with the Query
        Query personQuery = mDatabase.child("people").child(mPersonKey).child("pets");

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Pet>()
                .setQuery(personQuery, Pet.class)
                .build();

        mAdapter = new FirebaseRecyclerAdapter<Pet, PetViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PetViewHolder holder, int position, @NonNull Pet pet) {
                final DatabaseReference petRef = getRef(position);
                final String petKey = petRef.getKey();

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mPetKey = petKey;
                        OnFabAddPetClicked(null);
                    }
                });

                holder.bindToPost(pet, holder.itemView);
            }

            @Override
            public PetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                return new PetViewHolder(inflater.inflate(R.layout.row_pet, parent, false));
            }
        };
        mRecycler.setAdapter(mAdapter);
    }

    private void uploadPetWithPhoto() {
        if (ivPet == null)
            return;

        // UPLOAD IMAGE
        String imageChild = UUID.randomUUID().toString() + ".jpg";
        StorageReference photoRef = mStorageReference.child(imageChild);

        // Get the data from an ImageView as bytes
        ivPet.setDrawingCacheEnabled(true);
        ivPet.buildDrawingCache();
        final Bitmap bitmap = ivPet.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        // Create file metadata including the content type
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpg")
                .build();

        UploadTask uploadTask = photoRef.putBytes(data, metadata);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                bitmap.recycle();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                downloadUrl = taskSnapshot.getDownloadUrl();
                bitmap.recycle();
                uploadPet();
            }
        });
    }

    private void uploadPet() {
        if (mDialog == null)
            return;

        // ADD PET
        //noinspection ConstantConditions
        String name =((EditText) mDialog.getCustomView().findViewById(R.id.etName)).getText().toString();
        String age = ((EditText) mDialog.getCustomView().findViewById(R.id.etAge)).getText().toString();
        String thumbnail = downloadUrl == null ? "" : downloadUrl.toString();

        Pet pet = new Pet(name, TextUtils.isEmpty(age) ? 0 : Integer.valueOf(age), thumbnail);

        String key = mPersonReference.child("pets").push().getKey();
        if (!TextUtils.isEmpty(mPersonKey) && !TextUtils.isEmpty(mPetKey)) {
            key = mPetKey;
        }

        Map<String, Object> petValues = pet.toMap();
        mPersonReference.child("pets").child(key).updateChildren(petValues);

        String message = getString(R.string.created_pet);
        if (!TextUtils.isEmpty(mPersonKey) && !TextUtils.isEmpty(mPetKey)) {
            message = getString(R.string.updated_pet);
        }

        downloadUrl = null;
        mPetKey = "";
        mDialog.dismiss();

        Toast.makeText(AddActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private boolean IsValidPerson() {
        boolean isValid = true;

        if (TextUtils.isEmpty(etName.getText().toString())) {
            isValid = false;
            etName.setError(getString(R.string.this_field_is_required));
        }

        if (TextUtils.isEmpty(etPhone.getText().toString())) {
            isValid = false;
            etPhone.setError(getString(R.string.this_field_is_required));
        }

        return isValid;
    }
}
