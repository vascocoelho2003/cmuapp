package com.example.cmuapp.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cmuapp.data.repository.DocariaStats
import com.example.cmuapp.data.repository.RankingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RankingViewModel @Inject constructor(
    private val repository: RankingRepository
) : ViewModel() {

    private val _ranking = MutableLiveData<List<DocariaStats>>()
    val ranking: LiveData<List<DocariaStats>> = _ranking

    fun loadRanking(context: Context) {
        viewModelScope.launch {
            val top10 = repository.getTopDocarias(context)
            _ranking.value = top10
        }
    }
}

