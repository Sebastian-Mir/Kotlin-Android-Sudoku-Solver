package com.example.sudokusolver

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.renderscript.ScriptGroup
import android.util.Log
import android.widget.TextView
import com.example.sudokusolver.databinding.ActivityMainBinding
import java.lang.Integer.parseInt

class MainActivity : AppCompatActivity() {
    private var _binding : ActivityMainBinding? = null
    private val binding get() = _binding!!
    var grid = Array(9) { Array(9){"0"} }
    var errorSolving = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnSolveSudoku.setOnClickListener { solveSudoku() }
        binding.btnResetSudoku.setOnClickListener { resetSudoku() }
    }

    private fun resetSudoku(){
        binding.btnSolveSudoku.isEnabled = true
        var resourceName : String
        var resourceView : TextView
        var id: Int

        for(row in 0..8){
            for(col in 0..8){
                resourceName = "cell" + row + "_" + col
                id = this.resources.getIdentifier(resourceName, "id", packageName);
                resourceView = findViewById(id)
                resourceView.text = ""
            }
        }
    }

    //Starts the main process of solving the sudoku
    private fun solveSudoku(){
        errorSolving = false
        binding.btnSolveSudoku.isEnabled = false
        //copy the view values in the grid variable
        //change the values on the grid from "0" to "123456789"
        getGridValues() //only executed once
        //check for errors in the input numbers
        checkForErrors()
        //eliminate single values until there is no more single values
        Log.d("Printing", "Reduce Single Values")
        reduceSingleValues()
        //Check for unique values in row/col/9x9grid
        Log.d("Printing", "Search for unique values in the grid")
        findUniqueValues()
        //compare pairs in the same row/col/9x9grid
        findPairs()
        //check for errors
        checkForErrors()
        //Check if solved
        checkIfSolved()
        binding.btnResetSudoku.isEnabled = true
        //Final log print
        printLog()

    }

    //prints the sudoku in the logcat in debug with Printing tag
    private fun printLog(){
        var printing : String
        for(row in 0..8){
            printing = ""
            for(col in 0..8){
                printing += " ${grid[row][col]}"
            }
            Log.d("Printing", printing)
        }
    }

    //Obtains the values introduced by the user 1-9
    //0 is also possible as a number but it is handled as an empty value
    private fun getGridValues() {
        var resourceName : String
        var resourceText : TextView

        var id: Int
        for(row in 0..8){
            for(col in 0..8){
                resourceName = "cell" + row + "_" + col
                id = this.resources.getIdentifier(resourceName, "id", packageName);
                resourceText = findViewById(id)
                if (resourceText.text.isNullOrEmpty() || resourceText.text.toString() == "0"){//prevents the insertion of 0s
                    grid[row][col] = "123456789"
                } else {

                    grid[row][col] = resourceText.text.toString()
                }
            }
        }
        //printLog()
    }

    //compares individual values (like 1 not 123) in rows/columns/9x9grids and checks for repeated
    private fun checkForErrors(){
        if (errorSolving) return
        var rowToCompare : MutableList<String> //it becomes a 1 dimensional array of values to compare
        //check for errors in rows
        for (row in 0..8){
            rowToCompare = mutableListOf()
            for (col in 0..8){
                if (grid[row][col].length == 1) rowToCompare.add(grid[row][col])
            }
            if (rowToCompare.size >= 2){
                val section = "row $row"
                compareForErrors(rowToCompare, section)
                if (errorSolving) return //sequence of ending after finding the first error
            }
        }
        if (errorSolving) return
        //Checks for errors in columns
        for (col in 0..8){
            rowToCompare = mutableListOf() //empty list of values to compare
            for (row in 0..8){
                //if the value is single, it counts for comparison
                if (grid[row][col].length == 1) rowToCompare.add(grid[row][col])
            }
            if (rowToCompare.size >= 2){ //if we have more than two values to compare
                val section = "col $col" //message for the error handler with the type and number of column
                compareForErrors(rowToCompare, section)
                if (errorSolving) return //sequence of ending after finding the first error
            }
        }

        if (errorSolving) return
        //check for errors in 9x9 grids
        for (rowGrid in 0..2) {
            for (colGrid in 0..2) {
                rowToCompare = mutableListOf()
                for (row in 0..2) {
                    for (col in 0..2) {
                        if (grid[(rowGrid*3)+row][(colGrid*3)+col].length == 1)
                            rowToCompare.add(grid[(rowGrid*3)+row][(colGrid*3)+col])
                    }
                }
                if (rowToCompare.size >= 2) {
                    val section = "grid $rowGrid $colGrid"
                    compareForErrors(rowToCompare, section)
                    if (errorSolving) return //sequence of ending after finding the first error
                }
            }
        }
    }

    //compare the values obtained from checkForErrors function
    private fun compareForErrors(rowToCompare: MutableList<String>, section: String){
        //checks the values received, it is always a 9 or less array
        for (x in 0 until rowToCompare.size-1){
            for (y in (x+1) until rowToCompare.size){
                if (rowToCompare[x] == rowToCompare[y]){
                    errorSolving = true
                    errorHandler(section)
                }
            }
        }
    }

    ///display the error in case compareForErrors detect it
    private fun errorHandler(section: String){
        Log.d("Printing", "Error in: $section")
    }

    private fun reduceSingleValues(){
        //if there is errors then return
        if(errorSolving) return
        Log.d("Printing", "Reducing single values")
        //newSingles keeps track of new individual values
        var newSingles : Boolean

        do {
            newSingles = false
            //reduces the single values in rows
            for (row in 0..8){
                for (col in 0..8){
                    if (grid[row][col].length == 1){
                        val single = grid[row][col]
                        for (col2 in 0..8){
                            if (col != col2){
                                var nonSingle = grid[row][col2]
                                if (nonSingle.contains(single)){
                                    val newValue = nonSingle.replace(single, "")
                                    grid[row][col2] = newValue
                                    if (newValue.length == 1) newSingles = true
                                }
                            }
                        }
                    }
                }
            }

            //reduces the single values in columns
            for (col in 0..8){
                for (row in 0..8){
                    if (grid[row][col].length == 1){
                        val single = grid[row][col]
                        for (row2 in 0..8){
                            if (row != row2){
                                var nonSingle = grid[row2][col]
                                if (nonSingle.contains(single)){
                                    val newValue = nonSingle.replace(single, "")
                                    grid[row2][col] = newValue
                                    if (newValue.length == 1) newSingles = true
                                }
                            }
                        }
                    }
                }
            }

            //reduce the single values in grids
            for (rowGrid in 0..2) {
                for (colGrid in 0..2) {
                    for (row in 0..2) {
                        for (col in 0..2) {
                            val single = grid[(rowGrid*3)+row][(colGrid*3)+col]
                            if (single.length == 1){
                                for (row2 in 0..2) {
                                    for (col2 in 0..2) {
                                        if (!(row == row2 && col == col2)){
                                            val nonSingle = grid[(rowGrid*3)+row2][(colGrid*3)+col2]
                                            if (nonSingle.contains(single)){
                                                val newValue = nonSingle.replace(single, "")
                                                grid[(rowGrid*3)+row2][(colGrid*3)+col2] = newValue
                                                if (newValue.length == 1) newSingles = true
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            }
        } while (newSingles)

    }

    private fun findUniqueValues(){
        if(errorSolving) return
        var newSingles : Boolean
        do{
            newSingles = false
            ///unique values in rows
            for (row in 0..8) {
                var allNumbers = Array(9) { 0 }
                for (col in 0..8) {
                    if (grid[row][col].length > 1) {
                        for (number in grid[row][col].toList()) {
                            allNumbers[parseInt(number.toString()) - 1] += 1
                        }
                    }
                }
                //val unique = allNumbers.joinToString("")
                //Log.d("Printing", "String unique? = $unique")
                for (number in allNumbers.indices){
                    if (allNumbers[number] == 1) {
                        Log.d("Printing", "single value ${number+1} in row $row")
                        for (col in 0..8) {
                            if (grid[row][col].contains((number+1).toString())){
                                grid[row][col] = (number+1).toString()
                                newSingles = true
                            }
                        }
                    }
                }
            }
            if(newSingles) reduceSingleValues()

            ///unique values in columns
            for (col in 0..8) {
                var allNumbers = Array(9) { 0 }
                for (row in 0..8) {
                    if (grid[row][col].length > 1) {
                        for (number in grid[row][col].toList()) {
                            allNumbers[parseInt(number.toString()) - 1] += 1
                        }
                    }
                }
                //val unique = allNumbers.joinToString("")
                //Log.d("Printing", "String unique? = $unique")
                for (number in allNumbers.indices){
                    if (allNumbers[number] == 1) {
                        Log.d("Printing", "single value ${number+1} in col $col")
                        for (row in 0..8) {
                            if (grid[row][col].contains((number+1).toString())){
                                grid[row][col] = (number+1).toString()
                                newSingles = true
                            }
                        }
                    }
                }
            }
            if(newSingles) reduceSingleValues()

            //unique values in 9x9 grids
            for (rowGrid in 0..2) {
                for (colGrid in 0..2) {
                    var allNumbers = Array(9) { 0 }
                    for (row in 0..2) {
                        for (col in 0..2) {
                            if (grid[(rowGrid*3)+row][(colGrid*3)+col].length > 1) {
                                for (number in grid[(rowGrid*3)+row][(colGrid*3)+col].toList()) {
                                    allNumbers[parseInt(number.toString()) - 1] += 1
                                }
                            }
                        }
                    }
                    for (number in allNumbers.indices){
                        if (allNumbers[number] == 1) {
                            Log.d("Printing", "single value ${number+1} in 9x9 Grid $rowGrid _ $colGrid")
                            for (row in 0..2) {
                                for (col in 0..2) {
                                    if (grid[(rowGrid*3)+row][(colGrid*3)+col].contains((number + 1).toString())) {
                                        grid[(rowGrid*3)+row][(colGrid*3)+col] = (number + 1).toString()
                                        newSingles = true
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if(newSingles) reduceSingleValues()
        } while (newSingles)
    }

    private fun findPairs(){
        if(errorSolving) return
        var newSingles : Boolean
        var newPairs : Boolean
        do {
            newPairs = false
            newSingles = false
            ///pair values in columns
            for (row in 0..8) {

                for (col in 0..8) {
                    if (grid[row][col].length == 2) {
                        for (col2 in 0..8) {
                            if (col != col2 && grid[row][col2].length == 2) {
                                if (grid[row][col] == grid[row][col2]) {
                                    Log.d("Printing", "pair values ${grid[row][col]} in row $row")
                                    for (col3 in 0..8) {
                                        if (col3 != col && col3 != col2 &&
                                            grid[row][col3].length > 1
                                        ) {
                                            for (pairAsList in grid[row][col].toList()) {
                                                if (grid[row][col3].contains(pairAsList.toString())) {
                                                    val newValue = grid[row][col3].replace(
                                                        pairAsList.toString(),
                                                        ""
                                                    )
                                                    grid[row][col3] = newValue
                                                    if (newValue.length == 1) newSingles = true
                                                    if (newValue.length == 2) newPairs = true
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            //if new single values are produced
            if (newSingles) reduceSingleValues()
            //in case reducing the pairs and single values produces new unique values
            findUniqueValues()
            ///pair values in columns
            for (col in 0..8) {
                var pairs: String
                for (row in 0..8) {
                    if (grid[row][col].length == 2) {
                        for (row2 in 0..8) {
                            if (row != row2 && grid[row2][col].length == 2) {
                                if (grid[row][col] == grid[row2][col]) {
                                    Log.d("Printing", "pair values ${grid[row][col]} in col $col")
                                    for (row3 in 0..8) {
                                        if (row3 != row && row3 != row2 &&
                                            grid[row3][col].length > 1
                                        ) {
                                            for (pairAsList in grid[row][col].toList()) {
                                                if (grid[row3][col].contains(pairAsList.toString())) {
                                                    val newValue = grid[row3][col].replace(
                                                        pairAsList.toString(),
                                                        ""
                                                    )
                                                    grid[row3][col] = newValue
                                                    if (newValue.length == 1) newSingles = true
                                                    if (newValue.length == 2) newPairs = true
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (newSingles) reduceSingleValues()
            findUniqueValues()
            Log.d("Printing", "Searching for pairs in 3x3Grids")
            ///pair values in 9x9 Grids
            for (rowGrid in 0..2) {
                for (colGrid in 0..2) {
                    for (row in 0..2) {
                        for (col in 0..2) {
                            if (grid[(rowGrid * 3) + row][(colGrid * 3) + col].length == 2) {
                                Log.d(
                                    "Printing",
                                    "Searching ${grid[(rowGrid * 3) + row][(colGrid * 3) + col]}"
                                )
                                Log.d(
                                    "Printing",
                                    "In ${(rowGrid * 3) + row} _ ${(colGrid * 3) + col}"
                                )
                                for (row2 in 0..2) {
                                    for (col2 in 0..2) {
                                        if ((col != col2 || row != row2) &&
                                            grid[(rowGrid * 3) + row2][(colGrid * 3) + col2].length == 2
                                        ) {
                                            Log.d(
                                                "Printing",
                                                "Comparing with ${grid[(rowGrid * 3) + row2][(colGrid * 3) + col2]}"
                                            )
                                            Log.d(
                                                "Printing",
                                                "In ${(rowGrid * 3) + row2} _ ${(colGrid * 3) + col2}"
                                            )
                                            if (grid[(rowGrid * 3) + row][(colGrid * 3) + col] ==
                                                grid[(rowGrid * 3) + row2][(colGrid * 3) + col2]
                                            ) {
                                                Log.d(
                                                    "Printing",
                                                    "pair values ${grid[(rowGrid * 3) + row][(colGrid * 3) + col]} in 3x3Grid $rowGrid _ $colGrid"
                                                )
                                                for (row3 in 0..2) {
                                                    for (col3 in 0..2) {
                                                        if ((col3 != col || row3 != row) &&
                                                            (col3 != col2 || row3 != row2) &&
                                                            grid[(rowGrid * 3) + row3][(colGrid * 3) + col3].length > 1
                                                        ) {
                                                            for (pairAsList in grid[(rowGrid * 3) + row][(colGrid * 3) + col].toList()) {
                                                                if (grid[(rowGrid * 3) + row3][(colGrid * 3) + col3].contains(
                                                                        pairAsList.toString()
                                                                    )
                                                                ) {
                                                                    val newValue =
                                                                        grid[(rowGrid * 3) + row3][(colGrid * 3) + col3].replace(
                                                                            pairAsList.toString(),
                                                                            ""
                                                                        )
                                                                    grid[(rowGrid * 3) + row3][(colGrid * 3) + col3] =
                                                                        newValue
                                                                    if (newValue.length == 1) newSingles =
                                                                        true
                                                                    if (newValue.length == 2) newPairs =
                                                                        true
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (newSingles) reduceSingleValues()
            findUniqueValues()
        } while (newPairs)
    }

    private fun checkIfSolved(){
        if (errorSolving) return
        var solved = true
        for (row in 0..8){
            for (col in 0..8){
                if (grid[row][col].length != 1){
                    solved = false
                }
            }
        }
        if (solved){
            var resourceName : String
            var resourceView : TextView
            var id: Int

            for(row in 0..8){
                for(col in 0..8){
                    resourceName = "cell" + row + "_" + col
                    id = this.resources.getIdentifier(resourceName, "id", packageName);
                    resourceView = findViewById(id)
                    resourceView.text = grid[row][col]
                }
            }
        }
    }
}