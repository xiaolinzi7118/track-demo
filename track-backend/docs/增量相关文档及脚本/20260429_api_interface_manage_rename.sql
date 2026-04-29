START TRANSACTION;

UPDATE sys_menu
SET menu_name = '接口管理',
    update_time = NOW(3)
WHERE menu_code = 'api-interface'
  AND menu_type = 2;

COMMIT;
