package ru.spbstu.competition.game

import ru.spbstu.competition.protocol.Protocol
import ru.spbstu.competition.protocol.data.River
import java.util.*

class MinesAndRivers {
    companion object {
        val mapOfMines: TreeMap<Int, MutableMap<River, RiverState>> = TreeMap()
    }
}

class Intellect(val state: State, val protocol: Protocol) {

    fun makeMove() {


        if (MinesAndRivers.mapOfMines.isEmpty()) {
            state.mines.map { m ->
                val tryToFindNearRivers = state.rivers.filter { (river, riverState) ->
                    (river.source == m || river.target == m) && riverState == RiverState.Neutral
                }
                MinesAndRivers.mapOfMines[m] = tryToFindNearRivers.toMutableMap()
            }
        }
//        } else {
//            MinesAndRivers.mapOfMines.values.map { riverMap ->
//                riverMap.keys.map { river ->
//                    if (state.rivers[river] != RiverState.Neutral) {
//                        riverMap.remove(river)
//                    }
//                }
//            }
//        }

        val try0 = state.rivers.entries.find { (river, riverState) ->
            riverState == RiverState.Neutral && (river.source in state.mines && river.target in state.mines)
        }
        if (try0 != null) return move(try0.key.source, try0.key.target)

        // If there is a free river near a mine, take it!
        val try1 = state.rivers.entries.find { (river, riverState) ->
            riverState == RiverState.Neutral && (river.source in state.mines || river.target in state.mines)
        }
        if (try1 != null) return move(try1.key.source, try1.key.target)

        // Look at all our pointsees
        val ourSites = state.our.sites
//        val enemySites = state.enemy.sites

        // If there is a river between two our pointsees, take it!
        val try2 = state.rivers.entries.find { (river, riverState) ->
            riverState == RiverState.Neutral && (river.source in ourSites && river.target in ourSites)
        }
        if (try2 != null) return move(try2.key.source, try2.key.target)

        // If there is a river near our pointsee, take it!
        val try3 = state.rivers.entries.find { (river, riverState) ->
            riverState == RiverState.Neutral && (river.source in ourSites || river.target in ourSites)
        }
        if (try3 != null && !deadEnd(try3)) return move(try3.key.source, try3.key.target)

//        val try4 = state.rivers.entries.find { (river, riverState) ->
//            riverState == RiverState.Neutral && river.source in enemySites && river.target in enemySites
//        }
//        if (try4 != null) return move(try4.key.source, try4.key.target)
//
//        val try5 = state.rivers.entries.find { (river, riverState) ->
//            riverState == RiverState.Neutral && (river.source in enemySites || river.target in enemySites)
//        }
//        if (try5 != null) return move(try5.key.source, try5.key.target)

        // Bah, take anything left
        val try6 = state.rivers.entries.find { (_, riverState) ->
            riverState == RiverState.Neutral
        }
        if (try6 != null && !deadEnd(try6)) return move(try6.key.source, try6.key.target)

        // (╯°□°)╯ ┻━┻
        protocol.passMove()
    }

    private fun minePriority(state: State): List<Int> {
        val data = mutableMapOf<Int, Int>()
        val result = mutableListOf<Int>()
        for (mine in state.mines) {
            val rivers = state.rivers.entries.filter {
                (it.key.source == mine || it.key.target == mine) && it.value == RiverState.Neutral
            }
            data[mine] = rivers.size
        }
        var i = 1
        while (result.size != data.size) {
            for (obj in data)
                if (obj.value == i) result.add(obj.key)
            i++
        }
        return result
    }

    private fun deadEnd(river: MutableMap.MutableEntry<River, RiverState>): Boolean {
        val end = river.key.target
        val begin = river.key.target

        val filtered = state.rivers.filter { it.key.source == end || it.key.target == end }
        val ourTry = filtered.entries.find { (river, riverState) ->
            riverState == RiverState.Neutral && (river.source != begin || river.target != begin)
        }

        return ourTry == null
    }

    private fun move(source:Int, target:Int){
        protocol.claimMove(source, target)
    }

}
