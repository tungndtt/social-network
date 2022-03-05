
// help class to get k biggest elements from heap
class kHeap {
    constructor(){
        this.heap = []
        this.start = 0
    }

    // heapify the current heap
    // runtime O(logn)
    heapify(from){
        if(from === "BOTTOM"){
            let i = this.heap.length-1,
            next = Math.floor((i-1-this.start)/2);
            while(next >= 0 && this.heap[i][1] > this.heap[next+this.start][1]){
                let tmp = this.heap[i];
                this.heap[i] = this.heap[next+this.start];
                this.heap[next+this.start] = tmp;
                i = next+this.start;
                next = Math.floor((i-1-this.start)/2);
            }
        } 
        else {
            let i = this.start;
            while(2*i-this.start+1 < this.heap.length){
                let next = i;
                if(this.heap[2*i-this.start+1][1] > this.heap[i][1]){
                    let tmp = this.heap[i];
                    this.heap[i] = this.heap[2*i-this.start+1];
                    this.heap[2*i-this.start+1] = tmp;
                }
                if(2*i-this.start+2 < this.heap.length && this.heap[2*i-this.start+2][1] > this.heap[i][1]){
                    let tmp = this.heap[i];
                    this.heap[i] = this.heap[2*i-this.start+2];
                    this.heap[2*i-this.start+2] = tmp;
                }
                if(i == next) break;
                else i = next;
            }
        }
    }

    // add 2 new elements e1, e2 to heap
    // runtime O(logn)
    add(e1,e2){
        if(e1 != null || e2 != null){
            if(e1 != null){
                this.heap.push(e1);
                this.heapify("BOTTOM");
            }
            if(e2 != null){
                this.heap.push(e2);
                this.heapify("BOTTOM");
            }
            if(this.start < this.heap.length){
                return this.heap[this.start++];
            } 
            else {
                return null;
            }
        } else {
            if(this.start < this.heap.length){
                this.heapify("TOP")
                return this.heap[this.start++];
            } 
            else {
                return null;
            }
        }
    }
}

/*
    // test kHeap
    var k_heap = new kHeap();
    console.log(k_heap.add(["tung",9],null));
    console.log(k_heap.heap);
    console.log(k_heap.add(["ganmo",7],["nam",8]));
    console.log(k_heap.heap);
    console.log(k_heap.add(["id",1],["catchup",2]));
    console.log(k_heap.heap);
    console.log(k_heap.add(["four",4],["five",5]));
    console.log(k_heap.heap);
    console.log(k_heap.add(["leu",2],null));
    console.log(k_heap.heap);
    console.log(k_heap.add(["yo",3],["yo",3]));
    console.log(k_heap.heap);
    console.log(k_heap.add(null,null));
    console.log(k_heap.heap);
    console.log(k_heap.add(null,null));
    console.log(k_heap.heap);
    console.log(k_heap.add(null,null));
    console.log(k_heap.heap);
*/

// my Heap
class myHeap{
    constructor(){
        this.heap = [];
        this.index = {};
        this.holes = [];
    }

    // heapify from pos
    // runtime O(logn)
    heapify(pos){
        let i = Math.floor((pos-1)/2);
        if(i >= 0 && this.heap[i][1] < this.heap[pos][1]){
            while(i >= 0 && this.heap[i][1] < this.heap[pos][1]){
                this.index[this.heap[i][0]] = pos;
                let tmp = this.heap[pos];
                this.heap[pos] = this.heap[i];
                this.heap[i] = tmp;
                pos = i;
                i = Math.floor((pos-1)/2);
            }
            this.index[this.heap[pos][0]] = pos;
        }
        else {
            i = 2*pos+1 
            while(i<this.heap.length){
                if(i+1<this.heap.length && this.heap[i+1] != null){
                    if(this.heap[i] != null){
                        if(this.heap[i+1][1] > this.heap[i][1] && this.heap[i+1][1] > this.heap[pos][1]){
                            this.index[this.heap[i+1][0]] = pos;
                            let tmp = this.heap[pos];
                            this.heap[pos] = this.heap[i+1];
                            this.heap[i+1] = tmp;
                            pos = i+1;
                            i = 2*pos+1;
                        }
                        else if(this.heap[i][1] > this.heap[pos][1]){
                            this.index[this.heap[i][0]] = pos;
                            let tmp = this.heap[pos];
                            this.heap[pos] = this.heap[i];
                            this.heap[i] = tmp;
                            pos = i;
                            i = 2*pos+1;
                        }
                        else break;
                    }
                    else if(this.heap[i+1][1] > this.heap[pos][1]){
                        this.index[this.heap[i+1][0]] = pos;
                        let tmp = this.heap[pos];
                        this.heap[pos] = this.heap[i+1];
                        this.heap[i+1] = tmp;
                        pos = i+1;
                        i = 2*pos+1;
                    }
                    else break;
                }
                else if(this.heap[i] != null && this.heap[i][1] > this.heap[pos][1]){
                    this.index[this.heap[i][0]] = pos;
                    let tmp = this.heap[pos];
                    this.heap[pos] = this.heap[i];
                    this.heap[i] = tmp;
                    pos = i;
                    i = 2*pos+1;
                } 
                else break;
            }
            this.index[this.heap[pos][0]] = pos;
        }
    }

    // new a new element to heap
    // runtime O(logn)
    add(e){
        if(this.holes.length > 0){
            let i = this.holes.pop();
            this.heap[i] = e;
            this.index[e[0]] = i;
            this.heapify(i);
        } 
        else {
            let i = this.heap.length;
            this.heap.push(e);
            this.index[e[0]] = i;
            this.heapify(i);
        }
    }

    // remove the element with given id from heap
    // runtime O(logn)
    remove(id){
        if(this.heap[0] == null || this.index[id] === undefined) return;
        let i = this.index[id];
        while(2*i+1 < this.heap.length){
            if(2*i+2 < this.heap.length && this.heap[2*i+2] != null){
                if(this.heap[2*i+1] != null && this.heap[2*i+1][1] > this.heap[2*i+2][1]){
                    this.index[this.heap[2*i+1][0]] = i;
                    let tmp = this.heap[i];
                    this.heap[i] = this.heap[2*i+1];
                    this.heap[2*i+1] = tmp;
                    i = 2*i+1;
                } 
                else if(this.heap[2*i+1] != null){
                    this.index[this.heap[2*i+2][0]] = i;
                    let tmp = this.heap[i];
                    this.heap[i] = this.heap[2*i+2];
                    this.heap[2*i+2] = tmp;
                    i = 2*i+2;
                }
                else break;
            } 
            else if(this.heap[2*i+1] != null){
                this.index[this.heap[2*i+1][0]] = i;
                let tmp = this.heap[i];
                this.heap[i] = this.heap[2*i+1];
                this.heap[2*i+1] = tmp;
                i = 2*i+1;
            } 
            else break;
        }
        delete this.index[this.heap[i][0]];
        this.heap[i] = null;
        this.holes.push(i)
    }

    // update the element with given id according to val; action = {set[0], add[1]}
    // runtime O(logn)
    update(id, val, action){
        let i = this.index[id];
        if(i !== undefined){
            if(action === 0){
                this.heap[i][1] = val;
                this.heapify(i);
            } 
            else {
                this.heap[i][1] += val;
                this.heapify(i);
            }
        }
    }

    // get score of provided id in heap. Exist, return score ; otherwise, return null
    // runtime O(1)
    getScoreOf(id){
        let index = this.index[id];
        if(index !== undefined) return this.heap[index][1];
        else return null;
    }

    // get k maximal elements from heap
    // runtime O(k.logk)
    getKMax(k){
        let ans = [],
            k_heap = new kHeap();
        if(k > 0){
            let e = k_heap.add(this.heap[0], null);
            if(e != null) ans.push(e)
            k--; 
            while(k > 0 && e !== null){
                let i = this.index[e[0]],
                    e1 = 2*i+1 < this.heap.length ? this.heap[2*i+1] : null,
                    e2 = 2*i+2 < this.heap.length ? this.heap[2*i+2] : null;
                e = k_heap.add(e1,e2);
                if(e !== null) ans.push(e);
                k--;
            }
        }
        return ans;
    }
}

module.exports = {
    Heap: myHeap,
};

/*
    // test myHeap
    var heap = new myHeap();
    heap.add(["1", 1]);
    console.log(heap.heap)
    heap.add(["8", 8]);
    console.log(heap.heap)
    heap.add(["4", 4]);
    console.log(heap.heap)
    console.log("index", heap.index)
    heap.add(["9", 9]);
    console.log(heap.heap)
    heap.add(["3", 1]);
    console.log(heap.heap)
    heap.add(["2", 2]);
    console.log(heap.heap)
    console.log("index", heap.index)
    heap.add(["5", 5]);
    console.log(heap.heap)
    heap.update("3",5, 1)

    console.log(heap.getKMax(3))
    heap.remove("9")
    console.log(heap.heap)
    console.log("index", heap.index)
    heap.remove("8")
    console.log(heap.heap)
    console.log("index", heap.index)
    console.log(heap.getKMax(4))
    heap.add(["7",7])
    console.log(heap.heap)
    console.log("index", heap.index)
    heap.update("7",-1, 0)
    console.log(heap.heap)
    console.log("index", heap.index)
    heap.remove("7")
    console.log(heap.heap)
    console.log("index", heap.index)
    heap.add(["7",7])
    console.log(heap.heap)
    console.log(heap.getKMax(4));
*/