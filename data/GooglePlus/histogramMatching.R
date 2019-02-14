
dist = read.csv("dist_Makiyama.csv")
dist.dist = as.matrix(dist, labels=TRUE,)
dimnames(dist.dist) <- list(rownames(dist.dist, do.NULL = FALSE, prefix = ""), colnames(dist.dist, do.NULL = FALSE, prefix = ""))
input <- as.dist(dist.dist)
fit1 <- hclust(input, method="average")
library(NbClust)
res<-NbClust(diss=input, distance = NULL, min.nc=2, max.nc=20, method = "average", index = "silhouette")
plot(fit1)
bestFit <- res$Best.nc[1]
groups <- cutree(fit1, k=bestFit)
rect.hclust(fit1, k=bestFit, border="red")

den1 <- as.dendrogram(fit1)
den1 <- reorder(den1, 1:135)

source("manualClusteringDendrogram.R")

denManual <- as.dendrogram(a)
denManual <- reorder(denManual, 1:135)

library(dendextend)
#library(dendextendRcpp)
#library(colorspace)
#library(viridis)

dl1 <- dendlist(den1,denManual)

pdf("tanglegram1.pdf", width=25, height=25)

tanglegram(dl1, sort = TRUE,
	main_left = "PocketBench - Average", main_right = "Manual Grouping", main=entanglement(den1, denManual, L = 2), 
	common_subtrees_color_lines = TRUE, highlight_distinct_edges  = TRUE,
	highlight_branches_lwd = TRUE)
dev.off()

fit2 <- hclust(input, method="com")
den2 <- as.dendrogram(fit2)
den2 <- reorder(den2, 1:135)

dl2 <- dendlist(den2,denManual)

pdf("tanglegram2.pdf", width=25, height=25)

tanglegram(dl2, sort = TRUE,
	main_left = "PocketBench - Complete", main_right = "Manual Grouping", main=entanglement(den2, denManual, L = 2), 
	common_subtrees_color_lines = TRUE, highlight_distinct_edges  = TRUE,
	highlight_branches_lwd = TRUE)
dev.off()

fit3 <- hclust(input, method="single")
den3 <- as.dendrogram(fit3)
den3 <- reorder(den3, 1:135)

dl3 <- dendlist(den3,denManual)

pdf("tanglegram3.pdf", width=25, height=25)

tanglegram(dl3, sort = TRUE,
	main_left = "PocketBench - Single", main_right = "Manual Grouping", main=entanglement(den3, denManual, L = 2), 
	common_subtrees_color_lines = TRUE, highlight_distinct_edges  = TRUE,
	highlight_branches_lwd = TRUE)
dev.off()

fit4 <- hclust(input, method="centroid")
den4 <- as.dendrogram(fit4)
den4 <- reorder(den4, 1:135)

dl4 <- dendlist(den4,denManual)

pdf("tanglegram4.pdf", width=25, height=25)

tanglegram(dl4, sort = TRUE,
	main_left = "PocketBench - Centroid", main_right = "Manual Grouping", main=entanglement(den4, denManual, L = 2), 
	common_subtrees_color_lines = TRUE, highlight_distinct_edges  = TRUE,
	highlight_branches_lwd = TRUE)
dev.off()

fit5 <- hclust(input, method="ward.D2")
den5 <- as.dendrogram(fit5)
den5 <- reorder(den5, 1:135)

dl5 <- dendlist(den5,denManual)

pdf("tanglegram5.pdf", width=25, height=25)

tanglegram(dl5, sort = TRUE,
	main_left = "PocketBench - Ward.D2", main_right = "Manual Grouping", main=entanglement(den5, denManual, L = 2), 
	common_subtrees_color_lines = TRUE, highlight_distinct_edges  = TRUE,
	highlight_branches_lwd = TRUE)
dev.off()

