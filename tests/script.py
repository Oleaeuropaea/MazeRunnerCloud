import urllib.request

print('Printing')

maze = [50, 100, 250, 300, 500, 750, 1000]
stepSize = 5
velocity = 50
strategy = 'astar'
IP = '127.0.0.1'
PORT = '8000'


for m in maze:
    for i in range(1, m):
        for j in range (1, m):
            xFinish = (j + stepSize) % m
            yFinish = i if (j + stepSize < m) else i + 1
            print('Printed {0} {1} -> {2} {3}'.format(i, j, yFinish, xFinish))
            url = 'http://{0}:{1}/mzrun.html?m=Maze{2}.maze&x0={3}&y0={4}&x1={5}&y1={6}&v={7}&s={8}'.format(IP, PORT, m, j, i, xFinish, yFinish, velocity, strategy)
            print(url)
            try:
                with urllib.request.urlopen(url) as response:
                    html = response.read()
                    print(html)
            except urllib.error.HTTPError as error:
                print(error)


