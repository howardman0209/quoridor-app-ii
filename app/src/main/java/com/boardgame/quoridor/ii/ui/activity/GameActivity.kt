package com.boardgame.quoridor.ii.ui.activity

import com.boardgame.quoridor.ii.R
import com.boardgame.quoridor.ii.databinding.ActivityGameBinding
import com.boardgame.quoridor.ii.ui.activity.basic.BasicActivity

class GameActivity : BasicActivity<ActivityGameBinding>() {
    override fun getLayoutResId(): Int = R.layout.activity_game

}