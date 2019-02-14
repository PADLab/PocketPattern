import numpy as np
import matplotlib as mpl
import matplotlib.pyplot as plt
from scipy.interpolate import griddata

def reverse_colourmap(cmap, name = 'my_cmap_r'):
     return mpl.colors.LinearSegmentedColormap(name, plt.cm.revcmap(cmap._segmentdata)) 


# Load data from CSV
dat = np.genfromtxt('ettuExperiment2ver2.csv', delimiter=',',skip_header=0)
X_dat = dat[:,0]
Y_dat = dat[:,1]
Z_dat = dat[:,2]

# Convert from pandas dataframes to numpy arrays
X, Y, Z, = np.array([]), np.array([]), np.array([])
for i in range(len(X_dat)):
        X = np.append(X,X_dat[i])
        Y = np.append(Y,Y_dat[i])
        Z = np.append(Z,Z_dat[i])

# create x-y points to be used in heatmap
xi = np.linspace(X.min(),X.max(),1000)
yi = np.linspace(Y.min(),Y.max(),1000)

# Z is a matrix of x-y values
zi = griddata((X, Y), Z, (xi[None,:], yi[:,None]), method='nearest')

# I control the range of my colorbar by removing data 
# outside of my range of interest
zmin = Z.min()
zmax = Z.max()
zi[(zi<zmin) | (zi>zmax)] = None


# Create the contour plot
CS = plt.contourf(xi, yi, zi, 15, cmap=reverse_colourmap(plt.cm.autumn, 'my_cmap_r'),
                  vmax=zmax, vmin=zmin)
plt.colorbar()  
#plt.show()
plt.savefig('userDifference.pdf')
plt.close()


