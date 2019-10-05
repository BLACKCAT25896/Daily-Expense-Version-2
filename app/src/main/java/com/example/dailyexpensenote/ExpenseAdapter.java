package com.example.dailyexpensenote;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    private Context context;
    private View getView;
    private List<Expense> expenseList;
    private DatabaseHelper helper;

    public ExpenseAdapter() {
    }

    public ExpenseAdapter(Context context, List<Expense> expenseList, DatabaseHelper helper) {
        this.context = context;
        this.expenseList = expenseList;
        this.helper = helper;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_item_layout, parent, false);
        getView = view;
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        final Expense currentExpense=expenseList.get(position);
        holder.expenseType.setText(currentExpense.getType());
        holder.date.setText(currentExpense.getDate());
        holder.amount.setText(""+currentExpense.getAmount());


        Toast.makeText(context, "Testing"+ currentExpense.getType(), Toast.LENGTH_SHORT).show();


        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {


            @Override
            public boolean onLongClick(final View v) {

                PopupMenu menu=new PopupMenu(context,v);  //set popup menu custom process
                menu.getMenuInflater().inflate(R.menu.menu_setting,menu.getMenu());
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.deleteItem:
                                //delete confirmation
                                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                                builder.setTitle("Are you sure to delete ?");
                                builder.setCancelable(false);
                                builder.setIcon(R.drawable.ic_delete_black_24dp);

                                //when click yes
                                builder.setPositiveButton("Yes",new DialogInterface.OnClickListener(){

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        helper=new DatabaseHelper(context);
                                        helper.deleteData(currentExpense.getId());
                                        expenseList.remove(position);
                                        notifyDataSetChanged();
                                        dialog.cancel();
                                    }
                                });

                                //when click no
                                builder.setNegativeButton("No",new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                                builder.create();
                                builder.show();




                                break;

                            case R.id.updateItem:

                                requestUpdate(currentExpense);   //when clic update popup button

                                break;



                            default:
                        }
                        return false;
                    }
                });
                menu.show();       //finally need to call show function for display popup menu


                return false;
            }
        });

    }


    private void requestUpdate(Expense currentExpense) {

        //pass urgument data and call update fragment from adapter

        Intent intent = new Intent(context, AddExpenseActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("type", currentExpense.getType());
        bundle.putString("id", String.valueOf(currentExpense.getId()));
        bundle.putString("date", currentExpense.getDate());
        bundle.putString("time", currentExpense.getTime());
        bundle.putString("amount", String.valueOf(currentExpense.getAmount()));
        context.startActivity(intent);


    }


    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView expenseType, amount, date;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);


            expenseType = itemView.findViewById(R.id.expenseName);
            date = itemView.findViewById(R.id.expenseDate);
            amount = itemView.findViewById(R.id.expenseAmount);
            image = itemView.findViewById(R.id.expenseImage);
        }
    }
}
