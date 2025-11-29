AUTHOR: LUKASZ GOLINSKI

Hello people! Thanks for downloading my CSV Data Analyzer, its already built so no fiddling around, you can just directly run it via "java analyzer.Main" in terminal VSC or wherever.


Feature wise, well.. Its got everything you could ever need, this includes:
Basic stats such as count, min, max, range, mean etc.
Quartiles and Percentiles
Central Tendancy
Dispersion etc.

It has many methods of visualization, such as Histogram and Line Chart, you choose whichever one you like.

It also has 4PMA and EMA for accurate data visualization.


SUPPORTED FILE TYPES:
CSV, TSV and TXT.


In terms of how the data shall be arranged, there are 2 examples in the ZIP file ready.


     Column Headers: First row should contain column names
    Numeric Data: Data values must be numeric (integers or decimals)
    One Value Per Cell: Each cell contains a single value
    Consistent Columns: Each row should have the same number of columns

The program automatically handles:

Currency symbols: $, €, £, ¥ are stripped
Percentage signs: % is removed
Thousand separators: 1,000,000 reads as 1000000
Negative in parentheses: (100) reads as -100
Empty cells: Skipped during analysis
Non-numeric values: Text columns are ignored (only numeric columns analyzed)
NA values: NA, N/A, null, -, . treated as empty

What NOT to Include

Merged cells: Cannot be parsed correctly
Multiple tables in one file: Only first table structure recognized
Formulas Raw values only, not Excel formulas
Mixed data in single column: Column should be all numeric or all text



Enjoy and have fun, wish the very best from me 😁
