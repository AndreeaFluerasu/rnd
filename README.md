### About indexes in PostgreSQL

The methods available in PostgreSQL for creating an index are:   
**B-tree, hash, GiST, SP-GiST, GIN and BRIN**.

**B-tree Method**  

    It's the default method used by posgreSQL when creating an index  

    It's the only one that supports ordered scan with ASC being the default ordering.  

    To specify the ordering of the index you can use the following clauses: ASC, DESC, NULLS FIRST, NULLS LAST.

    It can be applied to multiple columns:  
        For instance, if we have an index on (a, b, c), it would be used when the query condition includes (a, b, c), (a, b), (a, c).
        However, the index would not be used if the query condition doesn't include the column 'a'.  

    B-tree indexes perform optimally when there is a high distribution of values, meaning a large number of distinct values.  

    B-trees are used whenever the indexed column is involved in a comparison using one of these operators:
    < , <= , = , >= , > , BETWEEN, IN, IS NULL, IS NOT NULL

    For LIKE and ILIKE operators a b-tree cannot be always used: for instance, in case of LIKE the index is used for `LIKE 'something%'` but not for `LIKE '%something'`

****

Let's consider a table called 'credit_transactions' that contains millions of transactions.  
The query to select the latest 50 transactions for a specific beneficiary would be structured as follows:
> explain analyse   
> select * from db_indexes.credit_transactions  
> where beneficiary_public_identifier = 'fb6e0960-10eb-4214-8301-2467667e4f22'  
> order by id desc  
> limit 50;  

Adding a B-tree index on **(beneficiary_public_identifier ASC)** may be beneficial, 
but in certain scenarios the sorting of transactions would be performed in an explicit sort node. 
A more effective approach would be to create an index on (beneficiary_public_identifier ASC, id DESC). 
This index would allow the index scan node to retrieve the transactions already ordered by id, 
eliminating the need for an additional sorting step.  

**** 

Having an index on the primary key and an index on (beneficiary_public_identifier ASC), 
when running the above query, if the items that match the query condition are spread across the table, 
the query plan will use the index on the primary key **(credit_transactions_pkey)**:

    Limit  (cost=0.43..20.72 rows=50 width=140) (actual time=8342.530..9644.817 rows=50 loops=1)  
        -> Index Scan Backward using credit_transactions_pkey on credit_transactions  (cost=0.43..1380230.29 rows=3401618 width=140) (actual time=8342.527..9644.803 rows=50 loops=1)  
           Filter: (beneficiary_public_identifier = 'ae42c0a8-b443-428e-9dc0-fec124992041'::uuid)  
           Rows Removed by Filter: 6463842  
    Planning Time: 1.918 ms  
    Execution Time: 9645.543 ms  

****

Having an index on the primary key and an index on (beneficiary_public_identifier ASC),
when running the above query, if the items that match the query condition are located in the middle of the table
the query plan may choose to use the index on **beneficiary_public_identifier** and **separately sort the transactions***.
  
    Limit  (cost=630.12..630.25 rows=50 width=140) (actual time=2173.806..2173.828 rows=50 loops=1)  
        ->  Sort  (cost=630.12..630.51 rows=157 width=140) (actual time=2173.805..2173.817 rows=50 loops=1)  
            Sort Key: id DESC  
            Sort Method: top-N heapsort  Memory: 51kB  
            ->  Bitmap Heap Scan on credit_transactions  (cost=5.65..624.91 rows=157 width=140) (actual time=591.923..1987.979 rows=1500001 loops=1)  
                Recheck Cond: (beneficiary_public_identifier = '38146145-ba11-4e74-92c6-9b34a88e1bb5'::uuid)  
                Heap Blocks: exact=35556  
                ->  Bitmap Index Scan on idx_credit_transactions_beneficiary_public_identifier  (cost=0.00..5.61 rows=157 width=0) (actual time=550.892..550.892 rows=1500001 loops=1)  
                    Index Cond: (beneficiary_public_identifier = '38146145-ba11-4e74-92c6-9b34a88e1bb5'::uuid)  
    Planning Time: 1.293 ms  
    Execution Time: 2174.512 ms  
****
Having an index on the primary key and an index on (beneficiary_public_identifier ASC, id desc), when running the above query,
if the items that match the query condition are spread across the table, 
the query plan uses the index on **(beneficiary_public_identifier ASC, id desc)**:

    Limit  (cost=0.56..20.75 rows=50 width=140) (actual time=0.967..0.992 rows=50 loops=1)  
        ->  Index Scan using idx_credit_transactions_beneficiary_public_identifier_id_desc on credit_transactions  (cost=0.56..1366280.46 rows=3382790 width=140) (actual time=0.966..0.985 rows=50 loops=1)  
            Index Cond: (beneficiary_public_identifier = '38146145-ba11-4e74-92c6-9b34a88e1bb5'::uuid)  
    Planning Time: 5.477 ms  
    Execution Time: 1.028 ms  
****
Having an index on the primary key and an index on (beneficiary_public_identifier ASC, id desc), when running the above query,
if the items that match the query condition are located in the middle of the table, the query plan uses the index on **(beneficiary_public_identifier ASC, id desc)**
> Limit  (cost=0.56..182.03 rows=50 width=140) (actual time=0.068..0.160 rows=50 loops=1)  
>   ->  Index Scan using idx_credit_transactions_beneficiary_public_identifier_id_desc on credit_transactions  (cost=0.56..875.26 rows=241 width=140) (actual time=0.067..0.153 rows=50 loops=1)  
>   Index Cond: (beneficiary_public_identifier = '0b5f709c-85f4-4e12-95c4-4121b23141a8'::uuid)  
> Planning Time: 0.187 ms  
> Execution Time: 0.185 ms  


The transactions are returned in the DESC order by the idx_credit_transactions_beneficiary_public_identifier_id_desc index. 
There is a noticeable performance improvement when using the index on (beneficiary_public_identifier ASC, id desc). 

****

When testing an index, it is important to consider different scenarios where the items matching the query condition are located in various places within the table.
Here are three examples
* one item every 1000 transactions
* multiple items every 100 000 transactions
* millions of items in the middle of the table

****

**Generalized Inverted Index Method**

    GIN handles composite values and allows for efficient full-text search operations.  
    
    GIN stores a set of (key, postingList) pairs, where a posting list contains the row IDs where the key occurs.  
    
    Multiple posting lists can contain the same row ID, as an item can have multiple keys associated with it.  
    
    GIN indexes are compact and perform well when the same key appears multiple times.
    
    A multi-column GIN index can be used for query conditions involving any subset of the index's columns.  
    
    Unlike B-trees or GiST (Generalized Search Tree), the search effectiveness of a GIN index remains consistent regardless of which index column(s) the query conditions use.  
    This flexibility makes GIN indexes particularly useful for scenarios involving full-text search or composite values.


Let's consider a scenario where a beneficiary wants to retrieve transactions based on a specific value in the reference field.
In our scenario, a GIN index would be suitable because the reference column is of text type, and we want to perform a full text search.
> explain analyse   
> select * from db_indexes.credit_transactions  
> where beneficiary_public_identifier = 'fb6e0960-10eb-4214-8301-2467667e4f22'  
> and reference like '%40a3%'  
> order by id desc  
> limit 50;  

Let's add a GIN index on (beneficiary_public_identifier, reference gin_trgm_ops) and run the above query:

>CREATE EXTENSION btree_gin;  
>CREATE EXTENSION pg_trgm;  

>CREATE INDEX CONCURRENTLY idx_credit_transactions_ben_pid_reference_gin  
>ON db_indexes.credit_transactions USING gin (beneficiary_public_identifier, reference gin_trgm_ops);  

    Limit  (cost=1646.60..1646.72 rows=50 width=140) (actual time=393.000..393.012 rows=50 loops=1)   
        ->  Sort  (cost=1646.60..1647.60 rows=400 width=140) (actual time=392.998..393.006 rows=50 loops=1)  
            Sort Key: id DESC  
            Sort Method: top-N heapsort  Memory: 51kB  
            ->  Bitmap Heap Scan on credit_transactions  (cost=68.10..1633.31 rows=400 width=140) (actual time=95.302..391.931 rows=1865 loops=1)  
                Recheck Cond: ((beneficiary_public_identifier = '0b5f709c-85f4-4e12-95c4-4121b23141a8'::uuid) AND ((reference)::text ~~* '%40a3%'::text))  
                Rows Removed by Index Recheck: 137  
                Heap Blocks: exact=1983  
                ->  Bitmap Index Scan on idx_credit_transactions_ben_pid_reference_gin  (cost=0.00..68.00 rows=400 width=0) (actual time=94.016..94.016 rows=2002 loops=1)  
                    Index Cond: ((beneficiary_public_identifier = '0b5f709c-85f4-4e12-95c4-4121b23141a8'::uuid) AND ((reference)::text ~~* '%40a3%'::text))  
    Planning Time: 0.156 ms  
    Execution Time: 393.052 ms  

*** 

### About query plans:

    The structure of a query plan is a tree of plan nodes.

    The scan nodes are positioned at the bottom of the tree and are responsible for returning raw rows directly from the table.  

    There are various types of scan nodes, including sequential scan nodes, index scan nodes, and bitmap index scan nodes.  

    Above the scan nodes, there may be additional nodes in the query plan that perform various operations such as joining, sorting, filtering, or aggregating the raw rows obtained from the scan nodes.  

    These nodes are responsible for processing and manipulating the data to fulfill the requirements of the query.  

    The bitmap index scan operates in conjunction with a Bitmap Heap Scan in a query plan.  

    The bitmap index scan constructs a bitmap that represents the locations of rows matching the index condition.  

    Bitmap Heap Scan decodes the bitmap and fetches the corresponding rows from the table.  

    The purpose of the Bitmap Heap Scan node is to organize and sort the row locations before retrieving the actual rows from the table.  

    Parallel queries include a Gather or Gather Merge node.  
        > Gather Merge node indicates that each process executing the parallel portion of the plan is producing tuples in sorted order, and that the leader is performing an order-preserving merge.  
        > Gather reads tuples from the workers in any convenient order.

***

    If the query requires joining two or more relations there are 3 join strategies available:  

        Nested loop join
            > The right relation is scanned once for every row found in the left relation  
            > If the planner decides to optimize the query plan by "materializing" the right relation, it introduces a Materialize node.   
              Its purpose is to store the data retrieved from the right relation in memory.  
              This means that the right relation scan is performed only once, 
              even though the nested loop join node may need to access that data multiple times for each row returned by the left relation scan.

        Merge join 
            > Each relation is sorted on the join attributes before the join starts.  
            > The sorting is either done by an index scan node or is an explicit step
            > The two relations are scanned in parallel and the matching rows are combined to form join rows.

        Hash join
            > The right relation is first scanned and loaded into a hash table using its join attributes as hash keys
            > Next, the left relation si scanned and the join attributes are used as hash key to locate the matching rows in the table.

