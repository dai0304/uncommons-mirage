SELECT *
	FROM /*$table*/some_table

	WHERE id > 0
		/*IF id != null*/
		AND id = /*id*/10
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
