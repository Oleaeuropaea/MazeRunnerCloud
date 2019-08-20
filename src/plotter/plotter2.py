import matplotlib.pyplot as plt
import numpy as np

x = [50, 100, 250, 300, 750, 1000]
yi = [11190911765, 19730834282, 15597069582, 27811202417, 45654013114, 77671028470]
yb = [1196359567, 2109393948, 1667327851, 2973148912, 4879874669, 8302106334]

xa = np.linspace(50,1000,100)

# plt.scatter(x,yi)
# regi = np.poly1d(np.polyfit(x, yi, 2))
# plt.plot(xa, regi(xa))
# plt.ylabel('number of instruction')
# plt.xlabel('distance')

plt.scatter(x,yb)
regb = np.poly1d(np.polyfit(x, yb, 2))
plt.plot(xa, regb(xa))
plt.ylabel('number of branches taken')
plt.xlabel('distance')

plt.show()
