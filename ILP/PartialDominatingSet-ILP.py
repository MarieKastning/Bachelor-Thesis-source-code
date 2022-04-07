#!/usr/bin/env python
# coding: utf-8

# ## Import Packages

# In[5]:


from gurobipy import *


# ## Graph Datastructure

# In[6]:


class Graph: 
    
    def __init__(self):
        self.vertices = []

    def add_vertex(self, vertex):
        if not self.check_ids(vertex):
            self.vertices.append(Vertex(vertex))
            
    def check_ids(self,vertex):
        for vert in self.vertices:
            if vert.id == vertex:
                return True
        return False
    
    def get_vert(self,vertex):
        for vert in self.vertices:
            if vert.id == vertex:
                return vert
        
    def add_edge(self, u, v):
        self.add_vertex(u)
        self.add_vertex(v)
        self.get_vert(u).add_neighbour(self.get_vert(v))

    def __str__(self):
        graph_string = ""
        for vertex in self.vertices:
            graph_string = graph_string + str(vertex) + ": { "
            for neighbour in vertex.neighbours:
                graph_string = graph_string + str(neighbour) + " "
            graph_string = graph_string + "} \n"
        return graph_string


# ## Vertex Class

# In[7]:


class Vertex:

    def __init__(self, id):
        if isinstance(id,int): 
            self.id = id
            self.neighbours = set()
            self.neighbours.add(self)
        else: print("id must be int")

    def add_neighbour(self, neighbour):
        if isinstance(neighbour, Vertex) and neighbour not in self.neighbours:
            self.neighbours.add(neighbour)
            neighbour.add_neighbour(self)

    def __str__(self):
        return f"{self.id}"
    
    def get_id(self):
        return self.id


# ## Graph einlesen

# In[15]:


import time
from argparse import ArgumentParser
parser = ArgumentParser(description='Will compute Partial Dominating Set of size <= k')
parser.add_argument('data', type=str, help='graphfile')
parser.add_argument('k', type=int, help='k')
args = parser.parse_args()
input
data = args.data
k = args.k


graph = Graph()
idMap = dict()

f = open(data,"r")
i = int(0)
for line in f:
    arr = line.split(" ")
    if  '%' not in arr[0]:
        e1 = arr[0].replace("\n", "")
        e2 = arr[1].replace("\n", "")
        if e1 not in idMap.keys():
            idMap[e1] = i
            i += 1
        if e2 not in idMap.keys():
            idMap[e2] = i
            i += 1  
f.close()

start= time.time()
f = open(data,"r")
for line in f:
    arr = line.split(" ")
    if '%' not in arr[0]:
        e1 = arr[0].replace("\n", "")
        e2 = arr[1].replace("\n", "")
        graph.add_edge(idMap[e1], idMap[e2])


# ## Model Creation

# In[ ]:


pds = Model(name="Partial Dominating set")
n = len(graph.vertices)

x = pds.addVars(n, vtype=GRB.BINARY, name = 'x')
c = pds.addVars(n, vtype=GRB.BINARY, name = 'c')

pds.setObjective(sum(c[i] for i in range(n)), GRB.MAXIMIZE)

pds.addConstr(sum(x[i] for i in range(n)) <= k)
pds.addConstrs(c[i] <= sum(x[j.id] for j in graph.vertices[i].neighbours) for i in range(n))


# In[ ]:


pds.optimize()
end = time.time()

if __name__=='__main__':
    outputname = data[5:] + "ILP.txt"
    datei = open(outputname,'a')
    output = "\n" + "k=" + str(k) + ": " + str(end-start) + "s"
    datei.write(str(output.encode('utf-8')))
    datei.close()

