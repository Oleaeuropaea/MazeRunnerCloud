# MazeRunner-Cloud
Project for the course of Cloud Computing &amp; Virtualization  during the 2017/18 academic year.

## Build, Run

## Test Request


`curl -v http://<load-balancer-DNS-name>/mzrun.html?m=<maze-filename>&x0=<x_start>&y0=<y_start>&x1=<x_final>&y1=<y_final>&v=<velocity>&s=<strategy>"`
* Examples
`http://35.171.129.176:8000/mzrun.html?m=Maze100.maze&x0=3&y0=9&x1=4&y1=13&v=50&s=astar`

, where mzrun.html points to the code receiving the request. The parameters express the maze we want run in, the entry coordinates, the exit coordinates, the speed, and the strategy to employ for maze solving.  
Recall that (0,0) is in top-left corner, the outside wall (grey wall) does not count to determine the position, speed should be within [1-100], and strategy is expressed with a string from these options: {bfs, dfs, astar}.


## Report

More information in [Project Report](https://https://github.com/Oleaeuropaea/MazeRunnerCloud/tree/master/docs/Report.pdf)

## Contributions

Luís Mendes luismurta@tecnico.ulisboa.pt

Henrique Alves henriquefalves@tecnico.ulisboa.pt

Gonçalo Garcia goncalotgarcia@tecnico.ulisboa.pt

Matheus Mello matheus.mello@tecnico.ulisboa.pt
