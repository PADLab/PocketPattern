import matplotlib.pyplot as plt
import numpy as np
from sklearn import datasets, linear_model

dat = np.genfromtxt('ettuExperiment1ver2.csv', delimiter=',',skip_header=0)

user = dat[:,0]
X_Date = dat[:,1]
Y_Result = dat[:,4]

firstIndex = 0
lastIndex = -1
counter = 0
labels = []

for i in range(int(user.min()), int(user.max() + 1)):
	counter = 0
	# Create linear regression object
	regr = linear_model.LinearRegression()
	#finding how many values there are for each user
	for j in range(0, len(user)):
		if user[j] == i:
			counter = counter + 1
	#Assuming the user column is ordered
	firstIndex = lastIndex + 1
	lastIndex = counter + lastIndex
	print(str(firstIndex) + ',' + str(lastIndex) + ',' + str(counter))
	if (counter == 0):
		continue
	t = np.array(X_Date[firstIndex:lastIndex])
	s = np.array(Y_Result[firstIndex:lastIndex])
	regr.fit(t[:, np.newaxis], s)
	#plt.scatter(t, s, marker='o', alpha=0.7)
	#std = np.std(s)
	plt.plot(t, regr.predict(t[:, np.newaxis]))
	labels.append(r'$User %i$' % (i))
	#plt.plot(t, regr.predict(t[:, np.newaxis]) + std)
	#labels.append('upper bound')
	#labels.append('observation')
plt.legend(labels, ncol=1, loc='upper right', 
           columnspacing=1.0, labelspacing=0.0,
           handletextpad=0.0, handlelength=1.5,
           fancybox=True, shadow=True)
plt.xlabel('Date')
plt.ylabel('KL-Divergence')
plt.title('Drift profile')
plt.grid(True)
plt.savefig('driftprofile.pdf')
plt.close()
	
#t = np.arange(X_Percent.min(), X_Percent.max(), 0.02)
#s = Y_Result
#plt.plot(t, s)

