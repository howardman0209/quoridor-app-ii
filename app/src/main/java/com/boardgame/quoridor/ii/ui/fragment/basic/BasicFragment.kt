package com.boardgame.quoridor.ii.ui.fragment.basic

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

abstract class BasicFragment<BINDING : ViewDataBinding> : Fragment() {
    protected val fragmentTag: String = this::class.java.simpleName

    @LayoutRes
    protected abstract fun getLayoutResId(): Int
    private var container: ViewGroup? = null

    protected val binding by lazy {
        DataBindingUtil.inflate(layoutInflater, getLayoutResId(), container, false) as BINDING
    }

    protected val applicationContext: Context by lazy {
        requireContext().applicationContext
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.container = container

        return binding.let {
            it.setBindingData()
            it.root
        }
    }

    open fun BINDING.setBindingData() {}
}