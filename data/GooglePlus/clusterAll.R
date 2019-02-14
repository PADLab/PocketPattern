args = commandArgs(trailingOnly=TRUE)

dist = read.csv(paste(args[1], "dist_Makiyama.csv", sep = "/"))

dist.dist = as.matrix(dist, labels=TRUE,)
dimnames(dist.dist) <- list(rownames(dist.dist, do.NULL = FALSE, prefix = ""), colnames(dist.dist, do.NULL = FALSE, prefix = ""))
input <- as.dist(dist.dist)
fit <- hclust(input, method="ward.D2")
#if (!require("NbClust")) install.packages("NbClust")
#library(NbClust)
#res<-NbClust(diss=input, distance = NULL, min.nc=2, max.nc=(ncol(dist.dist)/2), method = "ward.D2", index = "silhouette")
pdf(paste(args[1],"dendrogram.pdf", sep = "/"))
plot(fit)
bestFit <- 20
groups <- cutree(fit, k=bestFit)
rect.hclust(fit, k=bestFit, border="red")
dev.off()
write.table(groups, file=paste(args[1],"makiyama_clustered.txt", sep = "/"), row.names=FALSE, col.names=FALSE)
