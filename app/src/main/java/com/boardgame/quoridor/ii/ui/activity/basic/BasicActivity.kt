package com.boardgame.quoridor.ii.ui.activity.basic

import android.os.Bundle
import android.util.Log
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.lifecycleScope
import com.boardgame.quoridor.ii.ui.component.ProgressDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class BasicActivity<BINDING : ViewDataBinding> : AppCompatActivity() {
    protected val activityTag: String = this::class.java.simpleName

    @LayoutRes
    protected abstract fun getLayoutResId(): Int
    protected val binding by lazy<BINDING> {
        DataBindingUtil.setContentView(this, getLayoutResId())
    }

    private var progressStart = 0L
    private val progressDialog: AlertDialog by lazy {
        ProgressDialog(this).create()
    }

    protected open fun AlertDialog.init() {}

    open fun BINDING.setBindingData() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.setBindingData()
        progressDialog.init()
    }

    protected fun showLoadingIndicator(show: Boolean) {
        lifecycleScope.launch(Dispatchers.Main) {
            if (show) {
                progressDialog.show()
                progressStart = System.currentTimeMillis()
            } else {
                val processTime = System.currentTimeMillis() - progressStart
                Log.d("ProgressDialog", "processTime: $processTime")
                if (processTime < 500) {
                    delay(500L - processTime)
                    progressDialog.dismiss()
                } else {
                    progressDialog.dismiss()
                }
            }
        }
    }
}