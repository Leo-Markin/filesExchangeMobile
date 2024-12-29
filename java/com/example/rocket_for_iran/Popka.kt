package com.example.rocket_for_iran

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

class Popka(val list:List<Qwe>, val context: Context, val clik:(String)->Unit):RecyclerView.Adapter<Popka.Holder>() {
    class Holder(view: View):RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.fileName)
        val date = view.findViewById<TextView>(R.id.file_date)
        val size = view.findViewById<TextView>(R.id.file_size)
        val open = view.findViewById<ConstraintLayout>(R.id.my_const)
        val button_link = view.findViewById<Button>(R.id.copy_link_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
       return Holder(LayoutInflater.from(parent.context).inflate(R.layout.artochka,parent,false))
    }

    override fun getItemCount(): Int {
       return list.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.name.text = list[position].name
        holder.date.text = list[position].date
        holder.size.text = list[position].ansv
        val fileurl = list[position].link.substringAfterLast("/")
        holder.open.setOnClickListener{clik(fileurl)}
        holder.button_link.setOnClickListener({
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("link", list[position].link)
            clipboardManager.setPrimaryClip(clipData)
        })
    }

}