// https://github.com/janogonzalez/priorityqueuejs/blob/master/index.js
// Modified by Jae Shin, April 25, 2014.

/**
 * Initializes a new empty `PriorityQueue` with the given `comparator(a, b)`
 * function, uses `.DEFAULT_COMPARATOR()` when no function is provided.
 *
 * The comparator function must return a positive number when `a > b`, 0 when
 * `a == b` and a negative number when `a < b`.
 *
 * @param {Function}
 * @return {PriorityQueue}
 * @api public
 */
public class PriorityQueueElement {
    public double cost;
    public int x, y;
    public PriorityQueueElement(int x_, int y_, double cost_) {
       x = x_;
       y = y_;
       cost = cost_;
    }
}


public class PriorityQueue {
    Object _comparator;
    ArrayList _elements = new ArrayList();
    
    PriorityQueue(Object comparator) {
        //this._comparator = comparator;
        //this._elements = new Object[];
    }

    boolean isEmpty() {
        return this.size() === 0;
    }

    Object peek() {
        if (this.isEmpty()) throw new Error('PriorityQueue is empty');

        return this._elements.get(0);
    }

/**
 * Dequeues the top element of the priority queue.
 *
 * @return {Object}
 * @throws {Error} when the queue is empty.
 * @api public
 */
    Object deq() {
        Object first = this.peek();
        Object last = this._elements.remove(this.size()-1);
        Object size = this.size();

        if (size === 0) return first;
        
        this._elements.set(0, last);
        Object current = 0;
        
        while (current < size) {
            Object largest = current;
            Object left = (2 * current) + 1;
            Object right = (2 * current) + 2;
        
            if (left < size && this._compare(left, largest) > 0) {
              largest = left;
            }
            
            if (right < size && this._compare(right, largest) > 0) {
              largest = right;
            }
            
            if (largest === current) break;
            
            this._swap(largest, current);
            
            current = largest;
        }
        
        return first;
    }

/**
 * Enqueues the `element` at the priority queue and returns its new size.
 *
 * @param {Object} element
 * @return {Number}
 * @api public
 */
    Object enq(Object element) {
      _elements.add(element);
      int size = _elements.size();
      int current = size - 1;
    
      while (current > 0) {
        int parent = Math.floor((current - 1) / 2);
    
        if (this._compare(current, parent) < 0) break;
    
        this._swap(parent, current);
        current = parent;
      }
    
      return size;
    }

/**
 * Returns the size of the priority queue.
 *
 * @return {Number}
 * @api public
 */
    int size() {
        return this._elements.size();
    }

/**
 *  Iterates over queue elements
 *
 *  @param {Function} fn
 */
//PriorityQueue.prototype.forEach = function(fn) {
  //return this._elements.forEach(fn);
//};

/**
 * Compares the values at position `a` and `b` in the priority queue using its
 * comparator function.
 *
 * @param {Number} a
 * @param {Number} b
 * @return {Number}
 * @api private
 */
    double _compare(int a, int b) {
      //return this._comparator(this._elements[a], this._elements[b]);
        return _elements.get(b).cost - _elements.get(a).cost;
    };

/**
 * Swaps the values at position `a` and `b` in the priority queue.
 *
 * @param {Number} a
 * @param {Number} b
 * @api private
 */
    void _swap (Object a, Object b) {
        Object aux = this._elements.get(a);
        this._elements.set(a, this._elements.get(b));
        this._elements.set(b, aux);
    }
}
