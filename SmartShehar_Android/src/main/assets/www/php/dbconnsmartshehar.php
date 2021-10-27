<?php 
if(1==0){
$conn = mysql_connect('192.168.2.150', 'ssweb', 'OgChet789');
	if (!$conn) {
		die('Not connected : ' . mysql_error());
	}
	$db_selected = mysql_select_db('smartsheharbeta', $conn);
	if (!$db_selected) {
		die ('Can\'t use smartsheharbeta : ' . mysql_error());
	}
}	
else
{
$conn = mysql_connect('mysql10.ezhostingserver.com', 'smartshehar', 'OgChet789');
	if (!$conn) {
		die('Not connected : ' . mysql_error());
	}
	$db_selected = mysql_select_db('smartshehar_beta', $conn);
	if (!$db_selected) {
		die ('Can\'t use beta : ' . mysql_error());
	}
}
?>	
	