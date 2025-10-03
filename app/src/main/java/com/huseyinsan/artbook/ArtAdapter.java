package com.huseyinsan.artbook;

import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AlertDialogLayout;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.huseyinsan.artbook.databinding.RecyclerRowBinding;

import java.util.ArrayList;

public class ArtAdapter extends RecyclerView.Adapter<ArtAdapter.ArtHolder>{

    ArrayList<Art> artArrayList;
    MainActivity mainActivity;

    public ArtAdapter(ArrayList<Art> artArrayList, MainActivity mainActivity) {
        this.artArrayList = artArrayList;
        this.mainActivity = mainActivity;
    }

    @NonNull
    @Override
    public ArtHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ArtHolder(recyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtHolder holder, int position) {
        holder.binding.recyclerViewTextView.setText(artArrayList.get(position).name);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int currentPosition = holder.getAdapterPosition();
                if(currentPosition != RecyclerView.NO_POSITION) {
                    Intent intent = new Intent(holder.itemView.getContext(), ArtActivity.class);
                    intent.putExtra("info", "old");
                    intent.putExtra("artID", artArrayList.get(currentPosition).id);
                    holder.itemView.getContext().startActivity(intent);
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

               int currentPosition = holder.getAdapterPosition();

                if(currentPosition != RecyclerView.NO_POSITION) {
                    new AlertDialog.Builder(mainActivity).setTitle("Delete")
                            .setMessage(" ' " + artArrayList.get(currentPosition).name + " ' Are you sure to delete?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    int id = artArrayList.get(currentPosition).id;
                                    mainActivity.deleteArt(id, currentPosition);
                                }
                            }).setNegativeButton("No", null)
                            .show();
                }
                return true;
            }
        });


    }

    @Override
    public int getItemCount() {
        return artArrayList.size();
    }

    public class ArtHolder extends RecyclerView.ViewHolder{
        private RecyclerRowBinding binding;

        public ArtHolder(RecyclerRowBinding binding) {

            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
