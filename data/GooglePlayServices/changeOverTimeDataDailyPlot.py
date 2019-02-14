import matplotlib.pyplot as plt
import numpy as np

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
	#finding how many values there are for each user
	for j in range(0, len(user)):
		if user[j] == i:
			counter = counter + 1
	#Assuming the user column is ordered
	firstIndex = lastIndex + 1
	lastIndex = counter + lastIndex
	print(str(firstIndex) + ',' + str(lastIndex) + ',' + str(counter))
	t = np.array(X_Date[firstIndex:lastIndex])
	s = np.array(Y_Result[firstIndex:lastIndex])
	plt.plot(t, s, marker='o', alpha=0.7)
	labels.append(r'$User %i$' % (i))
	
#t = np.arange(X_Percent.min(), X_Percent.max(), 0.02)
#s = Y_Result
#plt.plot(t, s)

plt.legend(labels, ncol=1, loc='upper right', 
           columnspacing=1.0, labelspacing=0.0,
           handletextpad=0.0, handlelength=1.5,
           fancybox=True, shadow=True)

plt.xlabel('Date')
plt.ylabel('KL-Divergence')
plt.title('Change Over Time')
plt.grid(True)
plt.savefig('ChangeOverTimeDaily.pdf')
plt.close()