#!/bin/bash

# Array with maze numbers
mazeNumber=(100 250 300 500 750 1000)

# Array with maze numbers
points=(0 1 5 10 20 30 40 50 100 250 300 500 750 1000)

# Array with maze strategies
strategies=(bfs dfs astar)

sr=1
while [ $sr -lt 20 ];
do
	wr=1
	while [ $wr -lt 101 ];
	do
		# Seed random generator
		RANDOM=$$$(date +%s)

		# Get random maze numbers
	   	selectedMaze=${mazeNumber[$RANDOM % ${#mazeNumber[@]} ]}
		
		# Get random strategies
	   	selectedStrategies=${strategies[$RANDOM % ${#strategies[@]} ]}	

		# Get random strategies
	   	selectedPoints=${points[$RANDOM % ${#points[@]} ]}		

		# Random velocity
		velocity=$(( ( RANDOM % 100 )  + 1 ))
		
		# Random X start point
		xStart=$(( ( RANDOM % selectedMaze )))

		# Random Y start point
		yStart=$(( ( RANDOM % selectedMaze )))

		# Random X final point
		xFinal=$(( ( RANDOM % selectedMaze )))

		# Random Y final point
		yFinal=$(( ( RANDOM % selectedMaze )))
			
		echo Curl is require the url 
		echo It is $(((wr + sr) - 1)) times.
		echo Maze Selected is $selectedMaze 
		echo $selectedStrategies
		echo $xStart $xFinal $yStart $yFinal

		# Model
		# curl -X GET 'http://127.0.0.1:8000/mzrun.html?m=Maze100.maze&x0=3&y0=9&x1=4&y1=13&v=50&s=astar'

		curl -X GET 'http://127.0.0.1:8000/mzrun.html?m=Maze'$selectedMaze'.maze&x0='$xStart'&y0='$yStart'&x1='$xFinal'&y1='$yFinal'&v='$velocity'&s='$selectedStrategies'' 

		#curl -X GET 'http://127.0.0.1:8000/mzrun.html?m=Maze'$selectedMaze'.maze&x0='$selectedPoints'&y0='$selectedPoints'&x1='$selectedPoints'&y1='$selectedPoints'&v='$velocity'&s='$selectedStrategies''

		wr=$(( $wr+1))
		sleep 1
	done
	sr=$(( $sr+1))

done
