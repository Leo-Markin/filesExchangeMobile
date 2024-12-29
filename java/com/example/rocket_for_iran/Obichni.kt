package com.example.rocket_for_iran

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class Obichni (val list:List<Pipli>, val click: (Int) -> Unit, val changeclick: (Int) -> Unit):RecyclerView.Adapter<Obichni.Holder>(){
    class Holder(view: View): RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.user_name)
        val delbut = view.findViewById<Button>(R.id.button_delete_user)
        val changebut = view.findViewById<Button>(R.id.button_edit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.kartochka_usera,parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.name.text = list[position].name
        holder.delbut.setOnClickListener {
            click(list[position].id)
        }
        holder.changebut.setOnClickListener{
            changeclick(list[position].id)
        }
    }
}