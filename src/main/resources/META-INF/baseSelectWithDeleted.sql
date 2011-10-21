SELECT *
	FROM /*$table*/some_table

	/*BEGIN*/
	WHERE
		/*IF id != null*/
		id = /*id*/10
		/*END*/
	/*END*/
	
	/*BEGIN*/
	LIMIT
		/*IF offset != null*/
		/*offset*/0,
		/*END*/

		/*IF size != null*/
		/*size*/10
		/*END*/
	/*END*/
