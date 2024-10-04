package com.example.parakeet_application.utility

import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import com.example.parakeet_application.R
import com.example.parakeet_application.databinding.DialogLayoutBinding

class LoadingDialog(private val activity: Activity) {

    private var alertDialog: AlertDialog? = null

    fun startLoading() {
        if (!activity.isFinishing && (alertDialog == null || !alertDialog!!.isShowing)) {
            val builder = AlertDialog.Builder(activity, R.style.loadingDialogStyle)
            val binding = DialogLayoutBinding.inflate(LayoutInflater.from(activity), null, false)

            builder.setView(binding.root)
            builder.setCancelable(false)
            alertDialog = builder.create()
            alertDialog?.show()
        }
    }

    fun stopLoading() {
        alertDialog?.let{
            if (it.isShowing) {
                it.dismiss()
            }
        }
        alertDialog = null
    }

}