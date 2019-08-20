import csv
import matplotlib.pyplot as plt
import numpy as np
from math import sqrt, pow
from io import BytesIO

# threadID ;Maze file;X Initial;Y Initial;X Final;Y Final;Velocity;Strategy;
#  Basic Blocks Count;Load Count
runs = list()
with open('stats1+2_vdiff1+99.csv', 'r') as fp:
    reader = csv.DictReader(fp, delimiter=',')

    for row in reader:
        xi = row['X Initial'] = int(row['X Initial'])
        xf = row['X Final'] = int(row['X Final'])
        yi = row['Y Initial'] = int(row['Y Initial'])
        yf = row['Y Final'] = int(row['Y Final'])
        dist = sqrt(pow(xi - xf, 2) + pow(yi - yf, 2))
        row['Distance'] = dist

        row['Velocity'] = int(row['Velocity'])
        row['Basic Blocks Count'] = int(row['Basic Blocks Count'])
        row['Load Count'] = int(row['Load Count'])

        runs.append(row)

mazes = set(row['Maze file'][:-5] for row in runs)
strategies = set(row['Strategy'] for row in runs)
velocities = set(row['Velocity'] for row in runs)

print(mazes)

for maze in mazes:
    for strategy in strategies:
        # for velocity in velocities:
            strategyRuns = list(row for row in runs
                if row['Maze file'][:-5] == maze
                and row['Strategy'] == strategy)
                # and row['Velocity'] == velocity)

            if(len(strategyRuns) == 0):
                continue

            bbc_min = min(row['Basic Blocks Count'] for row in strategyRuns)
            bbc_max = max(row['Basic Blocks Count'] for row in strategyRuns)
            lc_min = min(row['Load Count'] for row in strategyRuns)
            lc_min = max(row['Load Count'] for row in strategyRuns)
            dist_min = round(min(row['Distance'] for row in strategyRuns), 1)
            dist_max = round(max(row['Distance'] for row in strategyRuns), 1)

            x = np.array(list(row['Distance'] for row in strategyRuns))
            y1 = np.array(list(row['Basic Blocks Count'] for row in strategyRuns))
            y2 = np.array(list(row['Load Count'] for row in strategyRuns))
            v = np.array(list(row['Velocity'] for row in strategyRuns))
            # v = velocity
            label = list('{0}'.format(i) for i in range(1, len(strategyRuns)+1))

            # if velocity == 99 or strategy == 'dfs':
            #     a1, b1 = np.polyfit(np.log(x+1), y1, 1)
            #     a2, b2 = np.polyfit(np.log(x+1), y2, 1)
            #     reg = 'Log'
            # elif maze == 'Maze250':
            #     reg1 = np.poly1d(np.polyfit(x, y1, 1))
            #     reg2 = np.poly1d(np.polyfit(x, y2, 1))
            #     reg = 'Linear'
            # elif strategy == 'astar' or strategy == 'bfs':
            #     reg1 = np.poly1d(np.polyfit(x, y1, 2))
            #     reg2 = np.poly1d(np.polyfit(x, y2, 2))
            #     reg = 'Quadratic'
            # # FOR LOG REGRESSION
            # a1, b1 = np.polyfit(np.log(x+1), y1, 1)
            # a2, b2 = np.polyfit(np.log(x+1), y2, 1)

            fig, ax1 = plt.subplots()

            ax1.scatter(x, y1, c='b')
            ax1.set_xlabel('Distance')
            ax1.set_ylabel('Basic Blocks Count', color='b')
            ax1.tick_params('y', colors='b')

            ax2 = ax1.twinx()
            ax2.scatter(x, y2, c='g')
            ax2.set_ylabel('Load Count', color='g')
            ax2.tick_params('y', colors='g')

            # x_array = np.linspace(dist_min, dist_max, 50)
            # if reg == 'Log':
            #     ## FOR LOG REGRESSION
            #     ax1.plot(x_array, np.log(x_array+1)*a1 + b1, '--k')
            #     ax2.plot(x_array, np.log(x_array+1)*a2 + b2, '--k')
            # else:
            #     ax1.plot(x_array, reg1(x_array), '--k')
            #     ax2.plot(x_array, reg2(x_array), '--k')


            for x, y1, y2, label in zip(x, y1, y2, v):
                ax1.annotate(
                    label,
                    xy=(x, y1), xytext=(-2,1),
                    textcoords='offset points', ha='right', va='bottom',
                    bbox=dict(boxstyle='round,pad=0.1', fc='gray', alpha=0.5),
                    arrowprops=dict(arrowstyle = '->', connectionstyle='arc3,rad=0')
                )
                ax2.annotate(
                    label,
                    xy=(x, y2), xytext=(2,1),
                    textcoords='offset points', ha='left', va='bottom',
                    bbox=dict(boxstyle='round,pad=0.1', fc='gray', alpha=0.5),
                    arrowprops=dict(arrowstyle = '->', connectionstyle='arc3,rad=0')
                )

            # title = str(maze) + ' Strategy ' + str(strategy) + \
            #     ' Velocity ' + str(velocity) + ' ' + reg + ' regression'
            # file = str(maze) + str(strategy) + \
            #     str(velocity) + reg

            title = str(maze) + ' Strategy ' + str(strategy)
            print(title)
            plt.title(title)
            fig.tight_layout()
            # plt.savefig(file + ".png", bbox_inches="tight")
            # plt.close()
            plt.show()
