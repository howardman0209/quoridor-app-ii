package com.boardgame.quoridor.ii.ui.component

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.core.graphics.drawable.toDrawable
import com.boardgame.quoridor.ii.R
import com.boardgame.quoridor.ii.databinding.DialogProgressBinding

class ProgressDialog(context: Context) : MaterialAlertDialogBuilder(context) {
    init {
        val layoutBinding: DialogProgressBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_progress, null, false)
        setView(layoutBinding.root)
        setCancelable(false)
        background = Color.TRANSPARENT.toDrawable()
    }

    override fun create(): AlertDialog {
        val dialog = super.create()
        dialog.window?.apply {
            setDimAmount(.4f)
            // for full screen dialog need to set the decorView padding to be 0
            // decorView.setPadding(0, 0, 0, 0)
        }
        return dialog
    }

}