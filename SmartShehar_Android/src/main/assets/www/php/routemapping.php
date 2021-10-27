<?php include("dbconnsmartshehar.php");
if( $conn )
{
	$routecode = $_REQUEST['routecode'];
	$direction = $_REQUEST['direction'];	
	$query = "call trackroute1(" . "'" . $routecode . "','" . $direction . "'" . ")";
	//echo $query;
	$result = mysql_query($query, $conn);
	$routemapdata = array();
	if($result) {
		while ($row = mysql_fetch_array($result)) {
			$routemapdata[] = array(
				'stopcode'=> $row['stopcode'],
				'stopname' => $row['stopname'],
				'stopnamedetail' => $row['stopnamedtl'],
				'lat' => $row['lat'],
				'lon' => $row['lon'],
				'routecode' => $row['routecode']
			);	
		}
	}	
	
	echo json_encode($routemapdata);
	
	// Close our MySQL Link
	mysql_close($conn);
	
}
?>	