/**
 * Canvas Render for the Maze Runner at the Cloud
 */

// Highly coupled with the output file produced by the MazeRunner
const PASSAGE_CHAR = ' ';
const WALL_CHAR = '#';
const BEDROCK_CHAR = '@';
const INITIAL_CHAR = 'I';
const FINAL_CHAR = 'X';
const VISITED_CHAR = ':';
const NEWLINE_CHAR = '\n';

const POSITION_SQUARE_SIZE = 1
const BEDROCK_COLOR = "black";
const WALL_COLOR = "brown";
const VISITED_COLOR = "green";
const INITIAL_COLOR = "red";
const FINAL_COLOR = "orange";

function renderSolvedMaze(positionSize) {
	var velocity = parseInt(document.getElementsByTagName("META")[2].content);
	var canvas = document.getElementById("solvedMaze");
	var ctx = canvas.getContext("2d");
	
	var mazeString = document.getElementById("maze").innerHTML;
	var x = 0;
	var y = 0;
	
	for(var i = 0; i < mazeString.length; i++){
		if(mazeString.charAt(i) == WALL_CHAR) {		
			ctx.beginPath();
			ctx.rect(x, y, positionSize, positionSize);
			ctx.fillStyle = WALL_COLOR;
			ctx.fill();
			ctx.closePath()
	        x += positionSize;
	        continue;
		}
		else if(mazeString.charAt(i) == VISITED_CHAR) {
			ctx.beginPath();
			ctx.rect(x, y, positionSize, positionSize);
			ctx.fillStyle = VISITED_COLOR;
			ctx.fill();
			ctx.closePath()
	        x += positionSize;
	        continue;
		}
		else if(mazeString.charAt(i) == PASSAGE_CHAR) {
			
	        x += positionSize;
	        continue;
		}
		else if(mazeString.charAt(i) == BEDROCK_CHAR) {
			ctx.beginPath();
			ctx.rect(x, y, positionSize, positionSize);
			ctx.fillStyle = BEDROCK_COLOR;
			ctx.fill();
			ctx.closePath()
	        x += positionSize;
	        continue;
		}
		else if(mazeString.charAt(i) == FINAL_CHAR) {
			ctx.beginPath();
			ctx.rect(x, y, positionSize, positionSize);
			ctx.fillStyle = FINAL_COLOR;
			ctx.fill();
			ctx.closePath()
	        x += positionSize;
	        continue;
		}
		else if(mazeString.charAt(i) == INITIAL_CHAR) {
			ctx.beginPath();
			ctx.rect(x, y, positionSize, positionSize);
			ctx.fillStyle = INITIAL_COLOR;
			ctx.fill();
			ctx.closePath()
	        x += positionSize;
	        continue;
		}
		else if(mazeString.charAt(i) == NEWLINE_CHAR) {
			x = 0;
			y += positionSize;
			continue;
		}
	}
	
	document.body.removeChild(document.body.childNodes[0]);
}

function createLogo(logoUrl) {
	var image = document.createElement("img");
	image.setAttribute("src",logoUrl);
	image.setAttribute("width","300pt");
	image.setAttribute("height","150pt");
	return image;
}

function renderResolution(width, height){
	if((width * height) <= 10000) {
		return 4;
	}
	else if((width * height) <= 62500) {
		return 2;
	}
	else{
		return 1;
	}
}

window.onload = function() {
	var width = parseInt(document.getElementsByTagName("META")[0].content);
	var height = parseInt(document.getElementsByTagName("META")[1].content);
	var positionSize = renderResolution(width,height);
	var canvasWidth = width * positionSize + (2 * positionSize);
	var canvasHeight = height * positionSize + (2 * positionSize);
	
	var svgCanvas = document.createElement("canvas");
	svgCanvas.setAttribute("id", "solvedMaze");
	svgCanvas.setAttribute("width",canvasWidth);
	svgCanvas.setAttribute("height",canvasHeight);
	
	document.body.appendChild(createLogo("https://grupos.ist.utl.pt/~meic-cnv.daemon/project/Logo.png"));
	document.body.appendChild(document.createElement("br"));
	document.body.appendChild(svgCanvas);
	
	renderSolvedMaze(positionSize)
	
};
