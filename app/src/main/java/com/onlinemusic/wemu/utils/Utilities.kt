package com.onlinemusic.wemu.utils


import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.onlinemusic.wemu.R


object Utilities {


    fun alertDialogUtil(
        context: Context?,
        title: String,
        message: String,
        isCancelable: Boolean = false,
        isPositive: Boolean = false,
        isNegetive: Boolean = false,
        isNeutral: Boolean = false,
        positiveTxt: String = "OK",
        negetiveTxt: String = "NO",
        neutralTxt: String = "Maybe",
        onItemClickListener: OnItemClickListener
    ) {
        val builder = AlertDialog.Builder(context!!)
        //builder.setTitle(title)
        //builder.setMessage(message)
        //builder.setIcon(R.drawable.logo)
        builder.setCancelable(isCancelable)
        val layoutAlert =LayoutInflater.from(context).inflate(R.layout.alertdialoglayout, null, false)
        builder.setView(layoutAlert);
        val dialog=builder.create()
        dialog.getWindow()!!.setBackgroundDrawableResource(R.drawable.dialogbackground);
        val dialogMessage= layoutAlert.findViewById<TextView>(R.id.dialogMessage)
        val btnDialog= layoutAlert.findViewById<RelativeLayout>(R.id.btnDialog)
        val tvDialog= layoutAlert.findViewById<TextView>(R.id.tvDialog)
        val twoBtnLL= layoutAlert.findViewById<RelativeLayout>(R.id.twoBtnLL)
        val btnYesDialog= layoutAlert.findViewById<RelativeLayout>(R.id.btnYesDialog)
        val tvYesDialog= layoutAlert.findViewById<TextView>(R.id.tvYesDialog)
        val btnNODialog= layoutAlert.findViewById<RelativeLayout>(R.id.btnNODialog)
        val tvNODialog= layoutAlert.findViewById<TextView>(R.id.tvNODialog)
        dialogMessage.text = message
        if(isPositive == true && isNegetive == true)
        {
            twoBtnLL.visibility= View.VISIBLE
            btnDialog.visibility= View.GONE
        }
        else
        {
            twoBtnLL.visibility= View.GONE
            btnDialog.visibility= View.VISIBLE
        }
        btnYesDialog.setOnClickListener {
            onItemClickListener.onItemClickAction(1, dialog)
        }
        btnNODialog.setOnClickListener {
            onItemClickListener.onItemClickAction(2, dialog)
            dialog.dismiss()
        }

        btnDialog.setOnClickListener {
            if (isPositive) {
                onItemClickListener.onItemClickAction(1, dialog)
            }
            if(isNegetive)
            {
                onItemClickListener.onItemClickAction(2, dialog)
            }
        }

        if (isPositive) {
            tvYesDialog.text=positiveTxt
            tvDialog.text=positiveTxt
            builder.setPositiveButton(positiveTxt) { dialog, which ->
                onItemClickListener.onItemClickAction(1, dialog)
                dialog.dismiss()
                /*Toast.makeText(
                    context,
                    positiveTxt, Toast.LENGTH_SHORT
                ).show()*/
            }
        }

        if (isNegetive) {
            tvNODialog.text=negetiveTxt
            tvDialog.text=negetiveTxt
            builder.setNegativeButton(negetiveTxt) { dialog, which ->
                onItemClickListener.onItemClickAction(2, dialog)
                Toast.makeText(
                    context,
                    negetiveTxt, Toast.LENGTH_SHORT
                ).show()
            }

        }
        if (isNeutral) {
            builder.setNeutralButton(neutralTxt) { dialog, which ->
                onItemClickListener.onItemClickAction(3, dialog)
                Toast.makeText(
                    context,
                    neutralTxt, Toast.LENGTH_SHORT
                ).show()
            }
        }
//builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))

        dialog.show()
    }


    interface OnItemClickListener {

        fun onItemClickAction(type: Int, dialogInterface: DialogInterface)
    }
    fun getDrawable(context: Context, id: Int): Drawable? {
        val version = Build.VERSION.SDK_INT
        return if (version >= 21) {
            ContextCompat.getDrawable(context, id)
        } else {
            context.resources.getDrawable(id)
        }
    }
}