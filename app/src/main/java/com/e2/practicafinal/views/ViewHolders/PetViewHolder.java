package com.e2.practicafinal.views.ViewHolders;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.e2.practicafinal.R;
import com.e2.practicafinal.models.Pet;
import com.e2.practicafinal.utilities.GlideApp;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by mcvasquez on 1/29/18.
 */

public class PetViewHolder extends RecyclerView.ViewHolder  {
    @BindView(R.id.tvName) TextView name;
    @BindView(R.id.tvAge) TextView age;
    @BindView(R.id.ivPet) ImageView ivPet;

    public PetViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bindToPost(Pet pet, View itemView) {
        name.setText(pet.name);
        age.setText(String.valueOf(pet.age));

        if (!TextUtils.isEmpty(pet.thumbnail))
            GlideApp.with(itemView).load(pet.thumbnail).circleCrop().into(ivPet);
    }
}
