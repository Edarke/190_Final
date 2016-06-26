# CSE-190-PathFinding-demo
Demonstrates path finding in randomly generated directed graphs. The problem is modeled as a car attempting to reach a destination given a finite amount of fuel and a finite number of refueling stations. The problem is not solvable by directly applying algorithms such as Djikstra's or Bellman-Ford because the optimal path may contain many cycles of arbitary size.  

This demo solves two variants of this problem:   
- Minimize total fuel spent  
- Maximize leftover fuel  



To visualize the algorithm, the map is color-coded (red squares represent high elevations, and blue squares represent low elevations). Traveling uphil costs a significant amount of fueld, whereas traveling on flat or downhill paths costs relatively little. 


