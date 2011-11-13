UPDATE /*$table*/some_table
	SET
		id = id * -1
	WHERE
		/*IF id != null*/
		id = /*id*/10
		/*END*/
