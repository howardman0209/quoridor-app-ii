package com.boardgame.quoridor.ii.ui.activity

import com.boardgame.quoridor.ii.R
import com.boardgame.quoridor.ii.databinding.ActivityHomeBinding
import com.boardgame.quoridor.ii.ui.activity.basic.BasicActivity

class HomeActivity : BasicActivity<ActivityHomeBinding>() {
    override fun getLayoutResId(): Int = R.layout.activity_home
}