function inArray(list, value) {
	var result = false;
	$.each(list, function(i, v) {
		if (v == value) { result = true; }
	});
	return result;
}

function csvToSentenceCase(str) {
	$.map( str.split(','), function( val, i ) {
		return val.toLowerCase; //.charAt(0).toUpperCase() + val.slice(1)
	}).reduce(function(previous, current) {
		return previous + ', ' + current;
	});
}

var fiTableSorter = {
	theme: 'blue',
	dateFormat: "mmddyyyy", // other options: "ddmmyyyy" & "yyyymmdd"
	sortMultiSortKey: "shiftKey", // The key used to select more than one column for multi-column sorting.
	sortResetKey: 'ctrlKey', // key used to remove sorting on a column
	headers: {
		0: { sorter: "text" },
		1: { sorter: "text" },
		2: { sorter: "text" },
		3: { sorter: "text" },
		4: { sorter: "text" },
		5: { sorter: "text" },
		6: { sorter : "text" },
		7: { sorter : "text" }
	},
	ignoreCase: true,
	sortList: [[0, 0], [1, 0], [2, 0]], // initial sort order of the columns
	sortAppend: null, // default sort that is added to the end of the users sort selection.
	sortInitialOrder: "asc", // starting sort direction "asc" or "desc"
	sortReset: false, // third click on the header will reset column to default - unsorted
	sortRestart: false, // restart sort to "sortInitialOrder" when clicking on previously unsorted columns
	emptyTo: "bottom", // sort empty cell to bottom, top, none, zero
	stringTo: "max", // sort strings in numerical column as max, min, top, bottom, zero

	// apply widgets on tablesorter initialization
	initWidgets: true,
	widgets: ['zebra', 'columns'],
	widgetOptions: {
		zebra: ["ui-widget-content even", "ui-state-default odd"],
		// columns widget: change the default column class names primary is the 1st column sorted, secondary is the 2nd, etc
		columns: ["primary", "secondary", "tertiary"],
		filter_childRows: false,
		filter_columnFilters: true,
		filter_cssFilter: "tablesorter-filter",
		filter_functions: null,
		filter_hideFilters: false,
		filter_ignoreCase: true,
		filter_reset: null,
		filter_searchDelay: 300,
		filter_serversideFiltering: false,
		filter_startsWith: false,
		filter_useParsedData: false,
		resizable: true,
		saveSort: true,
		stickyHeaders: "tablesorter-stickyHeader"
	},

	// *** CALLBACKS *** // function called after tablesorter has completed initialization
	initialized: function (table) {},

	// *** CSS CLASS NAMES ***
	tableClass: 'tablesorter',
	cssAsc: "tablesorter-headerSortUp",
	cssDesc: "tablesorter-headerSortDown",
	cssHeader: "tablesorter-header",
	cssHeaderRow: "tablesorter-headerRow",
	cssIcon: "tablesorter-icon",
	cssChildRow: "tablesorter-childRow",
	cssInfoBlock: "tablesorter-infoOnly",
	cssProcessing: "tablesorter-processing",

	// *** SELECTORS *** // jQuery selectors used to find the header cells.
	selectorHeaders: '> thead th, > thead td',

	// jQuery selector of content within selectorHeaders that is clickable to trigger a sort.
	selectorSort: "th, td",

	// rows with this class name will be removed automatically before updating the table cache - used by "update", "addRows" and "appendCache"
	selectorRemove: "tr.remove-me",

	// *** DEBUGING *** // send messages to console
	debug: false
}

var fiTableSorterPager = {
	container: $(".pager"), // use this url format "http:/mydatabase.com?page={page}&size={size}" 
	ajaxProcessing: function(ajax) {
		if (ajax && ajax.hasOwnProperty('data')) { return [ajax.data, ajax.total_rows]; }
	},
		
	// output string - default is '{page}/{totalPages}'; possible variables:
	// {page}, {totalPages}, {startRow}, {endRow} and {totalRows}
	output: '{startRow} to {endRow} ({totalRows})',

	updateArrows: true, // apply disabled classname to the pager arrows when the rows at either extreme is visible - default is true
	page: 0, // starting page of the pager (zero based index)
	size: 20, // Number of visible rows - default is 10

	// if true, the table will remain the same height no matter how many
	// records are displayed. The space is made up by an empty 
	// table row set to a height to compensate; default is false 
	fixedHeight: true,

	// remove rows from the table to speed up the sort of large tables.
	// setting this to false, only hides the non-visible rows; needed
	// if you plan to add/remove rows with the pager enabled.
	removeRows: false,

	// css class names of pager arrows
	cssNext: '.next', // next page arrow
	cssPrev: '.prev', // previous page arrow
	cssFirst: '.first', // go to first page arrow
	cssLast: '.last', // go to last page arrow
	cssGoto: '.gotoPage', // select dropdown to allow choosing a page
	cssDisabled: 'disabled' // location of where the "output" is displayed
}