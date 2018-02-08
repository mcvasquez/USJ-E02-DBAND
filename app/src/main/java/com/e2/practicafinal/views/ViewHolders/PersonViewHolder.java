package com.e2.practicafinal.views.ViewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.e2.practicafinal.R;
import com.e2.practicafinal.models.Person;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by mcvasquez on 1/29/18.
 */

public class PersonViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.tvName) TextView name;
    @BindView(R.id.tvPhone) TextView phone;
    @BindView(R.id.tvPetQuantity) TextView petQuantity;

    public PersonViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bindToPost(Person person) {
        name.setText(person.name);
        phone.setText(person.phone);
        petQuantity.setText(String.valueOf(person.pets.size()));
    }
}
