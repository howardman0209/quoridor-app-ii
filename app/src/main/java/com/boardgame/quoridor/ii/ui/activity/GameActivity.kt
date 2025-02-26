package com.boardgame.quoridor.ii.ui.activity

import android.os.Bundle
import com.boardgame.quoridor.ii.R
import com.boardgame.quoridor.ii.databinding.ActivityGameBinding
import com.boardgame.quoridor.ii.ui.activity.basic.BasicActivity
import com.boardgame.quoridor.ii.ui.fragment.GameFragment

class GameActivity : BasicActivity<ActivityGameBinding>() {
    override fun getLayoutResId(): Int = R.layout.activity_game


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.beginTransaction().apply {
            replace(binding.gameFragmentContainer.id, GameFragment())
            commit()
        }
    }
}