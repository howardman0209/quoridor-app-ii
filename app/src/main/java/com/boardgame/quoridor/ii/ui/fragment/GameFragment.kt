package com.boardgame.quoridor.ii.ui.fragment

import com.boardgame.quoridor.ii.R
import com.boardgame.quoridor.ii.databinding.FragmentGameBinding
import com.boardgame.quoridor.ii.ui.fragment.basic.BasicFragment

class GameFragment : BasicFragment<FragmentGameBinding>() {
    override fun getLayoutResId(): Int = R.layout.fragment_game
}