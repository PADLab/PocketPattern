import matplotlib.pyplot as plt
import numpy as np

dat = np.genfromtxt('ROC.txt', delimiter=',',skip_header=1)

#user = dat[:,0]
#X_Date = dat[:,1]
#Y_Result = dat[:,4]

driftPositives = dat[:,2]
driftTotalPositives = dat[:,3]
driftNegatives = dat[:,4]
driftTotalNegatives = dat[:,5]
tPositives = dat[:,6]
tTotalPositives = dat[:,7]
tNegatives = dat[:,8]
tTotalNegatives = dat[:,9]

driftTPR = []
rocDriftTPR = []
driftFPR = []
rocDriftFPR = []

tTPR = []
roctTPR = []
tFPR = []
roctFPR = []

sumDriftTPR = 0
sumDriftFPR = 0

sumtTPR = 0
sumtFPR = 0

for i in range(0, len(driftTotalPositives)):
	driftTPR.append(driftPositives[i]/driftTotalPositives[i])
	driftFPR.append(driftNegatives[i]/driftTotalNegatives[i])
	
for i in range(0, len(tTotalPositives)):
	tTPR.append(tPositives[i]/tTotalPositives[i])
	tFPR.append(tNegatives[i]/tTotalNegatives[i])
	
for i in range(0, len(driftTPR)):
	sumDriftTPR = sumDriftTPR + driftTPR[i]
	sumDriftFPR = sumDriftFPR + driftFPR[i]

for i in range(0, len(tTPR)):
	sumtTPR = sumtTPR + tTPR[i]
	sumtFPR = sumtFPR + tFPR[i]
	
convergeDriftTPR = 0
convergeDriftFPR = 0
rocDriftTPR.append(0)
rocDriftFPR.append(0)

convergetTPR = 0
convergetFPR = 0
roctTPR.append(0)
roctFPR.append(0)

for i in range(0, len(driftTPR)):
	convergeDriftTPR = convergeDriftTPR + driftTPR[i]
	rocDriftTPR.append(convergeDriftTPR/sumDriftTPR)
	convergeDriftFPR = convergeDriftFPR + driftFPR[i]
	rocDriftFPR.append(convergeDriftFPR/sumDriftFPR)
	
for i in range(0, len(tTPR)):
	convergetTPR = convergetTPR + tTPR[i]
	roctTPR.append(convergetTPR/sumtTPR)
	convergetFPR = convergetFPR + tFPR[i]
	roctFPR.append(convergetFPR/sumtFPR)
	
#for i in range(0, len(rocDriftFPR)):
#	print(str(rocDriftFPR[i]) + " " + str(rocDriftTPR[i]))
	
#for i in range(0, len(roctFPR)):
#	print(str(roctFPR[i]) + " " + str(roctTPR[i]))
	
#roc_auc1 = auc(rocDriftFPR, rocDriftTPR)
#print(str(roc_auc1))

#roc_auc2 = auc(roctFPR, roctTPR)
#print(str(roc_auc2))

plt.title('Receiver Operating Characteristic')
#plt.plot(driftFPR, driftTPR, 'b', label='AUC = %0.2f'% roc_auc)
plt.plot(sorted(driftFPR), sorted(driftTPR), label="drift")
plt.plot(sorted(tFPR), sorted(tTPR), label="threshold")
plt.legend(loc='lower right')
#plt.plot([0,1],[0,1],'r--')
#plt.xlim([-0.1,1.2])
#plt.ylim([-0.1,1.2])
plt.ylabel('True Positive Rate')
plt.xlabel('False Positive Rate')
plt.show()



